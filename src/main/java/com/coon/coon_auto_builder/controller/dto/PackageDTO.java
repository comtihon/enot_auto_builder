package com.coon.coon_auto_builder.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class PackageDTO {
    @JsonProperty("build_id")
    private String buildId;
    private String namespace;
    private String name;
    private boolean success;
    private String path;
    @JsonProperty("build_date")
    private Date buildDate;

    public PackageDTO() {
    }

    public PackageDTO(String buildId, String name, String namespace) {
        this.namespace = namespace;
        this.name = name;
        this.buildId = buildId;
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(Date buildDate) {
        this.buildDate = buildDate;
    }
}
