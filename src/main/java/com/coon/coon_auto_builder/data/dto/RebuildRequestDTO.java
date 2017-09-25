package com.coon.coon_auto_builder.data.dto;

import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.model.BuildBO;

import java.util.Collections;
import java.util.Optional;

public class RebuildRequestDTO extends BuildRequestDTO {
    private String buildId;

    public RebuildRequestDTO(String buildId, BuildDAOService service) {
        this.buildId = buildId;
        this.service = service;
    }

    public String getBuildId() {
        return buildId;
    }

    @Override
    public void validate() throws Exception {
        Optional build = service.find(buildId);
        if (!build.isPresent()) {
            throw new Exception("No such build!");
        } else {
            BuildBO found = (BuildBO) build.get();
            this.ref = found.getPackageVersion().getRef();
            this.buildVersions = Collections.singletonList(found.getPackageVersion().getErlVersion());
            this.repository = new RepositoryDTO(found.getPackageVersion().getRepository().getUrl());
        }
    }
}
