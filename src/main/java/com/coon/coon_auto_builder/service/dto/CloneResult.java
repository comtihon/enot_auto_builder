package com.coon.coon_auto_builder.service.dto;

import lombok.Data;

import java.nio.file.Path;

@Data
public class CloneResult {
    private final String email;
    private final Path cloned;
}
