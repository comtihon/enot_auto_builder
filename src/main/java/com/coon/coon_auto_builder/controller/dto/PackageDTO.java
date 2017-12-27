package com.coon.coon_auto_builder.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Data
public class PackageDTO {
    @JsonProperty("build_id")
    private final String buildId;
    private final String namespace;
    private final String name;
    private final boolean success;
    private final String path;
    @JsonProperty("erl_version")
    private final String erlangVersion;
    private final String version;
    @JsonFormat(pattern="yyyy-MM-dd hh:mm")
    @JsonProperty("build_date")
    private final Date buildDate;
}
