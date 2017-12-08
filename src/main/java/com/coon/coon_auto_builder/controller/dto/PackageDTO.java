package com.coon.coon_auto_builder.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PackageDTO {
    @JsonProperty("build_id")
    private String buildId;
    private String namespace;
    private String name;
    private boolean success;
    private String path;
    @JsonProperty("build_date")
    private Date buildDate;

    public PackageDTO(String buildId, String name, String namespace) {
        this.namespace = namespace;
        this.name = name;
        this.buildId = buildId;
    }
}
