package com.enot.enot_auto_builder.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.List;

@Data
public class PackageVersionDTO {
    @Nullable
    @JsonProperty("version_id")
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

    @Nullable
    public String getErlVersion() {
        return erlVersion;
    }
}
