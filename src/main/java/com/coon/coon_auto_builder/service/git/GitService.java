package com.coon.coon_auto_builder.service.git;

import com.coon.coon_auto_builder.config.ServerConfiguration;
import com.coon.coon_auto_builder.service.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class GitService {

    @Autowired
    private ServerConfiguration configuration;

    @Autowired
    private GaugeService gaugeService;


    /**
     * /**
     * Clones repo, returns email of the ref's commit and cloned path
     *
     * @param fullName namespace/name
     * @param url      git repo url
     * @param refStr      tag to be cloned
     * @return clone result - path of cloned repo and author's email
     * @throws Exception if unable to clone
     */
    public ClonedRepo cloneRepo(String fullName, String url, String refStr) throws Exception {
        Path repoPath = repoPath(fullName, refStr);
        if (!repoPath.toFile().mkdirs()) {
            String msg = "clone failed, can't create " + repoPath;
            log.warn(msg);
            this.gaugeService.submit(Metrics.CLONE_FAIL.toString(), 1.0);
            throw new Exception(msg);
        }
        try (Git result = Git.cloneRepository()
                .setURI(url)
                .setDirectory(repoPath.toFile())
                .setBranch(refStr)
                .call()) {
            log.debug("Cloned {} to {}", url, result.getRepository().getDirectory());
            this.gaugeService.submit(Metrics.CLONE_OK.toString(), 1.0);
            Ref ref = result.getRepository().findRef(refStr);
            if (ref == null) {
                log.warn("No such tag {} for {}", fullName, refStr);
                throw new Exception(refStr + " not found.");
            }
            RevCommit commit = result.getRepository().parseCommit(ref.getObjectId());
            return new ClonedRepo(commit.getAuthorIdent().getEmailAddress(), repoPath);
        } catch (IOException | GitAPIException e) {
            log.warn("clone failed {}", e.getMessage());
            this.gaugeService.submit(Metrics.CLONE_FAIL.toString(), 1.0);
            throw e;
        }
    }

    public List<Path> getClonedPaths(String fullName, Set<String> refs) {
        List<Path> paths = new ArrayList<>(refs.size());
        refs.forEach(ref -> paths.add(repoPath(fullName, ref)));
        return paths;
    }

    private Path repoPath(String fullName, String ref) {
        return Paths.get(configuration.getTempPath(), fullName, ref);
    }
}
