package com.coon.coon_auto_builder.service.loader;

import com.coon.coon_auto_builder.service.build.Builder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileLoader implements Loader {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileLoader.class);
    private final String artifactsPath;

    FileLoader(String path) {
        String[] splitted = path.split("file:");
        artifactsPath = splitted[1];
    }

    @Override
    public String loadArtifact(Builder build) throws IOException {
        Path src, dest;
        dest = Paths.get(artifactsPath, build.getName(),
                build.getNamespace(),
                build.getRef(),
                build.getErlang(),
                build.getName() + ".cp");
        src = Paths.get(build.getBuildPath().toString(), build.getName() + ".cp");
        LOGGER.debug("Copy " + src + " to " + dest);
        FileUtils.copyFile(src.toFile(), dest.toFile());
        return dest.toString();
    }
}
