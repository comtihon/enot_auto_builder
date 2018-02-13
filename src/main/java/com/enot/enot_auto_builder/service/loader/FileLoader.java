package com.enot.enot_auto_builder.service.loader;

import com.enot.enot_auto_builder.service.Metrics;
import com.enot.enot_auto_builder.service.build.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileLoader implements Loader {
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
                build.getPackageName() + ".ep");
        src = Paths.get(build.getBuildPath().toString(), build.getPackageName() + ".ep");
        log.debug("Copy " + src + " to " + dest);
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
