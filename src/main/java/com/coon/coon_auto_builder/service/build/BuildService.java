package com.coon.coon_auto_builder.service.build;

import com.coon.coon_auto_builder.config.ServerConfiguration;
import com.coon.coon_auto_builder.data.dao.PackageVersionDAOService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
@Slf4j
public class BuildService {
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
    private PackageVersionDAOService pvDAOService;

    @Autowired
    private Loader loader;

    @Async("taskExecutor")
    @Transactional
    public void buildAsync(RepositoryDTO repo) {
        Repository repository = repositoryDAOService.getOrCreate(repo.getCloneUrl(), repo.getFullName());
        Map<String, List<String>> versions = formVersions(repo.getVersions());
        List<Build> builds = new ArrayList<>();
        try {
            for (String ref : versions.keySet()) {
                try {
                    log.debug("build {}:{}", repo.getFullName(), ref);
                    builds.addAll(buildRef(repository, versions, ref));
                } catch (Exception e) {
                    Build build = new Build(e.getMessage());
                    PackageVersion version = pvDAOService.getOrCreate(ref, "unknown", repo.getCloneUrl());
                    version.addBuild(build);
                    repository.addVersion(version);
                    builds.add(build);
                }
            }
        } finally {
            cleanClonedRepos(repo, versions.keySet());
            mailSender.sendReport(builds);
        }
    }

    private Map<String, List<String>> formVersions(List<PackageVersionDTO> packageVersions) {
        Map<String, List<String>> versions = new HashMap<>();
        for (PackageVersionDTO version : packageVersions) {
            String erlVersion = version.getErlVersion();
            if (versions.containsKey(version.getRef()) && erlVersion != null) {
                versions.get(version.getRef()).add(erlVersion);
            } else if (erlVersion != null) {
                versions.put(version.getRef(), new ArrayList<>(Collections.singletonList(erlVersion)));
            } else {
                versions.put(version.getRef(), new ArrayList<>());
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
     * @return success build or build with error information
     * @throws Exception in case of cloning repo or reading configuration
     */
    private List<Build> buildRef(
            Repository repo, Map<String, List<String>> versions, String ref) throws Exception {
        CloneResult cloned = gitService.cloneRepo(repo.getFullName(), repo.getUrl(), ref);
        Path repoPath = cloned.getCloned();
        Map<String, Object> projectConf = new HashMap<>();
        List<String> erlangs = formErlangForVersions(projectConf, versions, ref, repoPath);
        List<Build> results = new ArrayList<>();
        if (erlangs.isEmpty()) {
            log.warn("Nothing to build for {}, versions {}", repo, versions);
            PackageVersion version = pvDAOService.getOrCreate(ref, "unknown", repo.getUrl());
            version.setEmail(cloned.getEmail());
            Build build = new Build("No Erlang version found for this repo.");
            version.addBuild(build);
            repo.addVersion(version);
            results.add(build);
            return results;
        }
        for (String erlang : erlangs) {
            PackageVersion version = pvDAOService.getOrCreate(ref, erlang, repo.getUrl());
            version.setEmail(cloned.getEmail()); // can have different emails for different refs
            Builder builder = appContext.getBean(Builder.class, repoPath, erlang);
            try {
                builder.buildVersion(erlangs.size() != 1);
                String artifact = loadPackage(builder, projectConf, repo, ref);
                Build result = builder.getBuild(artifact, "");
                version.addBuild(result);
                results.add(result);
            } catch (Exception e) {
                e.printStackTrace();
                Build result = builder.getBuild(null, e.getMessage());
                version.addBuild(result);
                results.add(result);
            }
            repo.addVersion(version);
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
     */
    private List<String> formErlangForVersions(Map<String, Object> projectConf,
                                               Map<String, List<String>> versions,
                                               String ref,
                                               Path repoPath) {
        List<String> erlangs = versions.get(ref);
        if (erlangs.isEmpty()) {
            try {
                projectConf.putAll(FileHelper.readConfig(repoPath));
            } catch (IOException ignored) {
            }
            erlangs = FileHelper.parseErlangVsns(projectConf, configuration.getErlangVersion());
        }
        return erlangs;
    }

    private String loadPackage(Builder builder,
                               Map<String, Object> projectConf,
                               Repository repo,
                               String ref) throws IOException {
        builder.detectPackageName(projectConf);
        return loader.loadArtifact(builder.withName(repo.getFullName()).withRef(ref));
    }

    private void cleanClonedRepos(RepositoryDTO repo, Set<String> refs) {
        List<Path> cloned = gitService.getClonedPaths(repo.getFullName(), refs);
        for (Path path : cloned)
            try {
                FileHelper.deleteDir(path);
            } catch (IOException e) {
                log.warn("Can't clean {} for {}", path, repo);
            }
    }
}
