package com.coon.coon_auto_builder.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

class RepositoryDTO {
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("clone_url")
    private String cloneUrl;

    String getFullName() {
        return fullName;
    }

    String getCloneUrl() {
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
