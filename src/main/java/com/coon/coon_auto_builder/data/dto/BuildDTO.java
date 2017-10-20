package com.coon.coon_auto_builder.data.dto;

import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.PackageVersion;
import com.coon.coon_auto_builder.data.entity.Repository;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class BuildDTO implements Validatable {
    @NotEmpty
    @JsonProperty("build_id")
    private String buildId;

    private boolean result = true;

    private String message = "";

    @JsonProperty("artifact_path")
    private String artifactPath;

    @JsonProperty("created_date")
    private Date createdDate = new Date();

    public String getArtifactPath() {
        return artifactPath;
    }

    @Override
    public void onConflict(Repository found, RepositoryDAOService service) throws Exception {
    }

    @Override
    public RepositoryDTO onRebuild(@Nullable Build found) throws Exception {
        if (found == null) {
            throw new Exception("No such build!");
        } else { //TODO refactor. Remove hand mapping
            PackageVersion version = found.getPackageVersion();
            Repository repo = version.getRepository();
            String ref = version.getRef();
            String erl = version.getErlVersion();
            PackageVersionDTO pv = new PackageVersionDTO(ref, erl);
            String name = repo.getName();
            String namespace = repo.getNamespace();
            String url = repo.getUrl();
            return new RepositoryDTO(namespace + "/" + name, url, pv);
        }
    }

    @Override
    public void basicValidation(String secret) throws Exception {
    }

    @Override
    public @Nullable String getBuildId() {
        return buildId;
    }

    @Override
    public @Nullable String getFullName() {
        return null;
    }

    @Override
    public @Nullable String getCloneUrl() {
        return null;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
