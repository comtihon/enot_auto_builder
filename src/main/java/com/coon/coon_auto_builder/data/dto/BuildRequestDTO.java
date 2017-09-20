package com.coon.coon_auto_builder.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BuildRequestDTO {
    private String ref;
    @JsonProperty("ref_type")
    private String refType;
    @JsonProperty("repository")
    private RepositoryDTO repository;

    public String getRef() {
        return ref;
    }

    public String getRefType() {
        return refType;
    }

    public String getName() {
        return repository.getFullName();
    }

    public String getUrl() {
        return repository.getCloneUrl();
    }

    public RepositoryDTO getRepository() {
        return repository;
    }

    @Override
    public String toString() {
        return "BuildRequestDTO{" +
                "ref='" + ref + '\'' +
                ", refType='" + refType + '\'' +
                ", repository=" + repository +
                '}';
    }
}
