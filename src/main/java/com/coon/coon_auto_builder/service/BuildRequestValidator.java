package com.coon.coon_auto_builder.service;

import com.coon.coon_auto_builder.config.ServerConfiguration;
import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.dto.Validatable;
import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.Repository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class BuildRequestValidator extends AbstractService {

    @Autowired
    private ServerConfiguration configuration;

    @Autowired
    private RepositoryDAOService repositoryDAOService;

    @Autowired
    private BuildDAOService buildDAOService;


    /**
     * Allow:
     * 1. Request from github if signature presents and correct
     * 2. Rebuild existing build request
     * 3. Build existing repository
     * 4. New build (not from github) if doesn't have namespace/name clash with
     * existing repository.
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<ResponseDTO> validate(Validatable request) {
        try {
            request.basicValidation(configuration.getGitubSecret());
            Optional<Repository> repoCollision = findCollision(request);
            if (repoCollision.isPresent())
                request.onConflict(repoCollision.get(), repositoryDAOService);
            Optional<Build> build = findBuild(request.getBuildId());
            RepositoryDTO vaildated = request.onRebuild(build.orElse(null));
            return CompletableFuture.completedFuture(ok(vaildated));
        } catch (Exception e) {
            log.warn("Validation failed: request {}, error {}", request, e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(fail("Validation failed: " + e.getMessage()));
        }
    }

    /**
     * Find repository by name and namespace and check url equality
     *
     * @return null if no repo found or found repo has the same url
     */
    private Optional<Repository> findCollision(Validatable repository) throws Exception {
        String fullName = repository.getFullName();
        if (fullName == null)
            return Optional.empty();
        if (!fullName.contains("/"))
            throw new Exception("Wrong fullname format: " + fullName);
        String[] split = fullName.split("/");
        String name = split[1];
        String namespace = split[0];
        Optional<Repository> repo = repositoryDAOService.findByNameAndNamespace(name, namespace);
        if (repo.isPresent()) {
            String url = repo.get().getUrl();
            if (url.endsWith(".git")) // remove .git
                url = FilenameUtils.removeExtension(url);
            if (url.equals(repository.getCloneUrl()))
                return Optional.empty();
        }
        return repo;
    }

    /**
     * Try to find build
     */
    private Optional<Build> findBuild(@Nullable String buildId) {
        if (buildId == null)
            return Optional.empty();
        return buildDAOService.find(buildId);
    }
}
