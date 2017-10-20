package com.coon.coon_auto_builder.service.loader;

import com.coon.coon_auto_builder.service.Metrics;
import com.coon.coon_auto_builder.service.build.Builder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileLoader implements Loader {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileLoader.class);
    private final String artifactsPath;

    @Autowired
    private CounterService counterService;

    @Autowired
    private GaugeService gaugeService;

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
        try {
            FileUtils.copyFile(src.toFile(), dest.toFile());
        } catch (IOException e) {
            this.gaugeService.submit(Metrics.LOAD_FAIL.toString(), 1.0);
            throw e;
        }
        this.gaugeService.submit(Metrics.LOAD_OK.toString(), 1.0);
        this.counterService.increment(Metrics.LOAD_ALL.toString());
        return dest.toString();
    }
}
