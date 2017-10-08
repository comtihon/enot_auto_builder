package com.coon.coon_auto_builder.service.dto;

import java.nio.file.Path;

public class CloneResult {
    private final String email;
    private final Path cloned;

    public CloneResult(String email, Path cloned) {
        this.email = email;
        this.cloned = cloned;
    }

    public String getEmail() {
        return email;
    }

    public Path getCloned() {
        return cloned;
    }
}
