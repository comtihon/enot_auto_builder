package com.coon.coon_auto_builder.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RepositoryDTO {
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("clone_url")
    private String cloneUrl;

    public RepositoryDTO() {
    }

    public RepositoryDTO(String url) {
        this.cloneUrl = url;
    }

    public RepositoryDTO(String name, String url) {
        this.fullName = name;
        this.cloneUrl = url;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    @Override
    public String toString() {
        return "RepositoryDTO{" +
                "fullName='" + fullName + '\'' +
                ", cloneUrl='" + cloneUrl + '\'' +
                '}';
    }
}
