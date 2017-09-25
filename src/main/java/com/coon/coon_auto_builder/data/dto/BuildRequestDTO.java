package com.coon.coon_auto_builder.data.dto;

import com.coon.coon_auto_builder.data.dao.DaoService;
import com.coon.coon_auto_builder.data.model.BuildRequest;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import com.coon.coon_auto_builder.system.ServerConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;

/**
 * Manual json build/rebuild request
 */
public class BuildRequestDTO {
    @JsonIgnore
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String ref;
    @JsonProperty("ref_type")
    String refType;
    @JsonProperty("repository")
    protected RepositoryDTO repository;
    @Nullable
    @JsonProperty("erl_versions")
    List<String> buildVersions;

    @JsonIgnore
    DaoService service;

    public BuildRequestDTO() {
    }

    public BuildRequestDTO(String ref, String refType, RepositoryDTO repository,
                           @Nullable List<String> buildVersions, DaoService service) {
        this.ref = ref;
        this.refType = refType;
        this.repository = repository;
        this.buildVersions = buildVersions;
        this.service = service;
    }

    public String getRef() {
        return ref;
    }

    public String getName() {
        return repository.getFullName();
    }

    public String getUrl() {
        return repository.getCloneUrl();
    }

    public void setServiceOnce(DaoService service) {
        if (this.service == null)
            this.service = service;
    }

    public RepositoryDTO getRepository() {
        return repository;
    }

    public BuildRequest toBuildRequest(ApplicationContext appContext, ServerConfiguration configuration) {
        return appContext.getBean(BuildRequest.class, createRepository(configuration));
    }

    private RepositoryBO createRepository(ServerConfiguration configuration) {
        return new RepositoryBO(configuration.getTempPath(), getName(), getRef(), getUrl(), buildVersions);
    }

    @Override
    public String toString() {
        return "BuildRequestDTO{" +
                "ref='" + ref + '\'' +
                ", repository=" + repository +
                '}';
    }

    /**
     * In case of Repository with same name/namespace and different url exists in the system - throw error.
     *
     * @throws Exception if saving another url by these repo detected
     */
    public void validate() throws Exception {
        RepositoryBO repo = findCollision(service);
        if (repo != null) {
            logger.warn("Request " + this + " tries to overwrite " + repo);
            throw new Exception("Request " + this + " tries to overwrite " + repo);
        }
    }

    /**
     * Find repository by name and namespace and check url equality
     *
     * @param repositoryDAOService dao
     * @return null if no repo found or found repo has the same url
     */
    RepositoryBO findCollision(DaoService repositoryDAOService) {
        String[] split = repository.getFullName().split("/");
        String name = split[1];
        String namespace = split[0];
        Optional repo = repositoryDAOService.findByNameAndNamespace(name, namespace);
        if (!repo.isPresent()) { //No repo - nothing to overwrite
            return null;
        } else { //If found repo url differs - there is possible overwrite attempt
            RepositoryBO found = (RepositoryBO) repo.get();
            if (found.getUrl().equals(getUrl()))
                return null;
            return found;
        }
    }
}
