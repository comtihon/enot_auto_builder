package com.coon.coon_auto_builder.loader;

import com.coon.coon_auto_builder.domain.ErlPackage;
import com.coon.coon_auto_builder.jpa.service.ErlPackageServiceInterface;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileLoader implements Loader {
    private final String artifactsPath;

    @Autowired
    private ErlPackageServiceInterface packageService;

    FileLoader(String path) {
        String[] splitted = path.split("file:");
        artifactsPath = splitted[1];
    }

    @Override //TODO should be thread-safe
    public void loadArtifact(ErlPackage artifact) throws IOException {
        Path dest = Paths.get(artifactsPath, artifact.getSource().toString(), artifact.getName() + ".cp");
        Path source = Paths.get(artifact.getTempPath(), artifact.getName() + ".cp");
        FileUtils.copyFile(source.toFile(), dest.toFile());
        System.out.println("Copy " + artifact.getName() + " to " + dest);
        artifact.setPath(dest.toString());
        packageService.savePackage(artifact);
    }
}
