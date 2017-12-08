package com.coon.coon_auto_builder.data.dto;

import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.Repository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class RepositoryDTO implements Validatable {
    @NotEmpty
    @NonNull
    @JsonProperty("full_name")
    String fullName;
    @JsonProperty("clone_url")
    String cloneUrl;
    List<PackageVersionDTO> versions;
    @JsonProperty("ref_type")
    String refType;

    public RepositoryDTO(String url) {
        this.cloneUrl = url;
    }

    public RepositoryDTO(String name, String url, PackageVersionDTO pv) {
        this.fullName = name;
        this.cloneUrl = url;
        this.versions = Collections.singletonList(pv);
    }

    @NonNull
    public List<PackageVersionDTO> getVersions() {
        if (versions == null) {
            versions = new ArrayList<>();
        }
        return versions;
    }

    @Override
    public String toString() {
        return "RepositoryDTO{" +
                "fullName='" + fullName + '\'' +
                ", cloneUrl='" + cloneUrl + '\'' +
                '}';
    }

    /**
     * In case of Repository with same name/namespace and different url exists in the system - throw error.
     *
     * @throws Exception if saving another url by these repo detected
     */
    @Override
    public void onConflict(Repository found, RepositoryDAOService service) throws Exception {
        if (found != null) {
            log.warn("Request " + this + " tries to overwrite " + found);
            throw new Exception("Request " + this + " tries to overwrite " + found);
        }
    }

    @Override
    public RepositoryDTO onRebuild(@Nullable Build found) throws Exception {
        return this;
    }

    @Override
    public void basicValidation(String secret) throws Exception {
    }

    @Override
    public @Nullable String getBuildId() {
        return null;
    }
}
