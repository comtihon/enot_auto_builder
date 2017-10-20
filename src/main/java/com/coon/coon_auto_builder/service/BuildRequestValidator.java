package com.coon.coon_auto_builder.service;

import com.coon.coon_auto_builder.config.ServerConfiguration;
import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.dto.Validatable;
import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.Repository;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class BuildRequestValidator extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildRequestValidator.class);

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
            LOGGER.warn("Validation failed: request {}, error {}", request, e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(fail(e.getMessage()));
        }
    }

    /**
     * Find repository by name and namespace and check url equality
     *
     * @return null if no repo found or found repo has the same url
     */
    private Optional<Repository> findCollision(Validatable repository) {
        String fullName = repository.getFullName();
        if (fullName == null)
            return Optional.empty();
        String[] split = fullName.split("/");
        String name = split[1];
        String namespace = split[0];
        Optional<Repository> repo = repositoryDAOService.findByNameAndNamespace(name, namespace);
        if (repo.isPresent()) {
            if (repo.get().getUrl().equals(repository.getCloneUrl()))
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
