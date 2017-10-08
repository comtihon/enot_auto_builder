package com.coon.coon_auto_builder.service;

import com.coon.coon_auto_builder.config.ServerConfiguration;
import com.coon.coon_auto_builder.service.dto.CloneResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildSearchService.class);

    @Autowired
    private ServerConfiguration configuration;

    /**
     * /**
     * Clones repo, returns email of the ref's commit and cloned path
     *
     * @param fullName namespace/name
     * @param url      git repo url
     * @param ref      tag to be cloned
     * @return clone result - path of cloned repo and author's email
     * @throws Exception if unable to clone
     */
    public CloneResult cloneRepo(String fullName, String url, String ref) throws Exception {
        Path repoPath = Paths.get(configuration.getTempPath(), fullName, ref);
        if (!repoPath.toFile().mkdirs()) {
            String msg = "clone failed, can't create " + repoPath;
            LOGGER.warn(msg);
            throw new Exception(msg);
        }
        try (Git result = Git.cloneRepository()
                .setURI(url)
                .setDirectory(repoPath.toFile())
                .setBranch(ref)
                .call()) {
            LOGGER.debug("Cloned {} to {}", url, result.getRepository().getDirectory());
            RevCommit commit = result.getRepository().parseCommit(result.getRepository().findRef(ref).getObjectId());
            return new CloneResult(commit.getAuthorIdent().getEmailAddress(), repoPath);
        } catch (IOException | GitAPIException e) {
            LOGGER.warn("clone failed {}", e.getMessage());
            throw e;
        }
    }
}
