package com.enot.enot_auto_builder.service.tool;

import com.enot.enot_auto_builder.controller.dto.Renderable;
import com.enot.enot_auto_builder.tool.FileHelper;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
@Slf4j
@ToString
public class Erlang implements Renderable {

    private final String version;
    private final String path;
    private String releasePath; //path where packaged erlang release is stored
    private String artifactsPath; //

    public Erlang(String version, String path, String artifactsPath) {
        this.version = version;
        this.path = path;
        if (artifactsPath.startsWith("file:")) {
            String[] splitted = artifactsPath.split("file:");
            this.artifactsPath = splitted[1];
        } else
            this.artifactsPath = artifactsPath;
    }

    @Override
    public String getArtifactPath() {
        return releasePath;
    }

    /**
     * Copy erts to path and zip, fill path
     *
     * @return form result
     */
    public boolean formRelease() {
        Path release = Paths.get(artifactsPath, version + ".tar");
        if (!release.toFile().isFile()) {
            try {
                Path erts = getErts().toPath();
                FileHelper.compress(erts, release.toString());
            } catch (Exception e) {
                log.warn("Skip {} : {}", version, e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        this.releasePath = release.toString();
        return true;
    }

    @VisibleForTesting
    File getErts() {
        Optional<File> erts = Arrays.stream(
                Objects.requireNonNull(new File(path).listFiles()))
                .filter(f -> f.getName().startsWith("erts"))
                .findFirst();
        return erts.orElseThrow(() -> new RuntimeException("No erts for " + path));
    }
}
