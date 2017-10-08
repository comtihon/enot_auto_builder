package com.coon.coon_auto_builder.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PackageVersionDTO {
    @Nullable
    private String versionId;

    private String ref;

    @Nullable
    @JsonProperty("erl_version")
    private String erlVersion;

    @Nullable
    private List<BuildDTO> builds;

    public PackageVersionDTO(String ref, @Nullable String erlVersion) {
        this.ref = ref;
        this.erlVersion = erlVersion;
    }

    public PackageVersionDTO(String ref) {
        this.ref = ref;
    }

    public PackageVersionDTO() {
    }

    public String getRef() {
        return ref;
    }

    public String getErlVersion() {
        return erlVersion;
    }
}
