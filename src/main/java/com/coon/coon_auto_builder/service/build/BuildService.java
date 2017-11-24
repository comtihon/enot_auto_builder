package com.coon.coon_auto_builder.service.build;

import com.coon.coon_auto_builder.config.ServerConfiguration;
import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.PackageVersion;
import com.coon.coon_auto_builder.data.entity.Repository;
import com.coon.coon_auto_builder.service.GitService;
import com.coon.coon_auto_builder.service.MailSenderService;
import com.coon.coon_auto_builder.service.dto.CloneResult;
import com.coon.coon_auto_builder.service.loader.Loader;
import com.coon.coon_auto_builder.tool.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class BuildService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BuildService.class);

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private ServerConfiguration configuration;

    @Autowired
    private GitService gitService;

    @Autowired
    private MailSenderService mailSender;

    @Autowired
    private RepositoryDAOService repositoryDAOService;

    @Autowired
    private Loader loader;

    @Async("taskExecutor")
    public void buildAsync(RepositoryDTO repo) {
        Map<String, List<String>> versions = formVersions(repo.getVersions());
        List<PackageVersion> results = new ArrayList<>();
        try {
            for (String ref : versions.keySet()) {
                try {
                    results.addAll(buildRef(repo, versions, ref));
                } catch (Exception e) {
                    e.printStackTrace();
                    results.add(formError(ref, "unknown", e.getMessage()));
                }
            }
        } finally {
            Repository repository = new Repository(repo.getCloneUrl(), repo.getFullName(), results);
            repositoryDAOService.cascadeSave(repository);
            cleanClonedRepos(repo, versions.keySet());
            mailSender.sendReport(repository);
        }
    }

    private Map<String, List<String>> formVersions(List<PackageVersionDTO> packageVersions) {
        Map<String, List<String>> versions = new HashMap<>();
        for (PackageVersionDTO version : packageVersions) {
            if (versions.containsKey(version.getRef())) {
                versions.get(version.getRef()).add(version.getErlVersion());
            } else {
                versions.put(version.getRef(), new ArrayList<>(Collections.singletonList(version.getErlVersion())));
            }
        }
        return versions;
    }

    /**
     * Builds repo for current ref and all Erlang versions specified
     *
     * @param repo     repository to build
     * @param versions versions from build request
     * @param ref      ref being build
     * @return list of all builds
     * @throws Exception in case of cloning repo or reading configuration
     */
    private List<PackageVersion> buildRef(
            RepositoryDTO repo, Map<String, List<String>> versions, String ref) throws Exception {
        CloneResult cloned = gitService.cloneRepo(repo.getFullName(), repo.getCloneUrl(), ref);
        List<String> erlangs = formErlangForVersions(versions, ref, cloned.getCloned());
        List<PackageVersion> results = new ArrayList<>();
        if (erlangs.isEmpty()) {
            LOGGER.warn("Nothing to build for {}", repo);
            PackageVersion errored = formError(ref, "unknown", "No Erlang version found for this repo.");
            errored.setEmail(cloned.getEmail());
            results.add(errored);
            return results;
        }
        for (String erlang : erlangs) {
            PackageVersion version = new PackageVersion(ref, erlang);
            version.setEmail(cloned.getEmail()); // can have different emails for different refs
            Builder builder = appContext.getBean(Builder.class, cloned.getCloned(), erlang);
            try {
                builder.buildVersion(erlangs.size() != 1);
                String artifact = loader.loadArtifact(builder.withName(repo.getFullName()).withRef(ref));
                version.addBuild(builder.getBuild(artifact, ""));
            } catch (Exception e) {
                e.printStackTrace();
                version.addBuild(builder.getBuild(null, e.getMessage()));
            }
            results.add(version);
        }
        return results;
    }

    /**
     * Get Erlang versions to build from RepositoryDTO. If none specified - read them from cloned config
     *
     * @param versions versions from build request
     * @param ref      repository version
     * @param repoPath path to the cloned repo
     * @return Erlang versions to build with
     * @throws IOException in case of missing configuration in clone repo
     */
    private List<String> formErlangForVersions(
            Map<String, List<String>> versions, String ref, Path repoPath) throws IOException {
        List<String> erlangs = versions.get(ref);
        if (erlangs.isEmpty())
            erlangs = FileHelper.readConfig(repoPath, configuration.getErlangVersion());
        return erlangs;
    }

    private PackageVersion formError(String ref, String erl, String reason) {
        PackageVersion version = new PackageVersion(ref, erl);
        Build build = new Build(version, false, "");
        build.setMessage(reason);
        version.addBuild(build);
        return version;
    }

    private void cleanClonedRepos(RepositoryDTO repo, Set<String> refs) {
        List<Path> cloned = gitService.getClonedPaths(repo.getFullName(), refs);
        for (Path path : cloned)
            try {
                FileHelper.deleteDir(path);
            } catch (IOException e) {
                LOGGER.warn("Can't clean {} for {}", path, repo);
            }
    }
}
