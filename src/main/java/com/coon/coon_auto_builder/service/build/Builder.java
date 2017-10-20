package com.coon.coon_auto_builder.service.build;

import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.PackageVersion;
import com.coon.coon_auto_builder.service.Metrics;
import com.coon.coon_auto_builder.service.tool.Kerl;
import com.coon.coon_auto_builder.tool.CmdHelper;
import com.coon.coon_auto_builder.tool.FileHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Builder {
    private final Path repoPath;
    private Path buildPath;
    private String erlang;
    private String ref;
    private String name;
    private String namespace;

    @Autowired
    private Kerl kerl;

    @Autowired
    private GaugeService gaugeService;

    public Builder(Path repoPath, String erlang) {
        this.repoPath = repoPath;
        this.erlang = erlang;
    }

    void buildVersion(boolean copy) throws Exception {
        Map<String, String> compilers = kerl.getErlInstallations();
        String erlangExecutable = compilers.get(erlang);
        if (erlangExecutable == null) {
            this.gaugeService.submit(Metrics.BUILD_FAIL.toString(), 1.0);
            throw new Exception("no erlang installed");
        }
        try {
            mayBeCopy(copy);
        } catch (IOException e) {
            this.gaugeService.submit(Metrics.BUILD_FAIL.toString(), 1.0);
            throw new Exception("Can't copy from " + repoPath + " to " + buildPath + ": " + e.getMessage());
        }
        ProcessBuilder pb = new ProcessBuilder("coon", "package");
        pb.directory(buildPath.toFile());
        Map<String, String> env = pb.environment();
        String path = env.get("PATH");
        env.put("PATH", Paths.get(erlangExecutable, "bin").toString() + ":" + path);
        try {
            Process process = pb.start();
            if (process.waitFor() != 0) {
                this.gaugeService.submit(Metrics.BUILD_FAIL.toString(), 1.0);
                throw new Exception("build failed: " + CmdHelper.getProcessError(process));
            }
            this.gaugeService.submit(Metrics.BUILD_OK.toString(), 1.0);
        } catch (IOException | InterruptedException e) {
            this.gaugeService.submit(Metrics.BUILD_FAIL.toString(), 1.0);
            throw new Exception("build failed: " + e.getMessage());
        }
    }

    Build getBuild(String artifactPath, String message) {
        PackageVersion version = new PackageVersion(ref, erlang);
        Build build = new Build(version, artifactPath != null, artifactPath);
        build.setMessage(message);
        return build;
    }

    public Path getBuildPath() {
        return buildPath;
    }

    public String getErlang() {
        return erlang;
    }

    public String getRef() {
        return ref;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    Builder withName(String fullName) {
        String[] splitted = fullName.split("/");
        this.name = splitted[1];
        this.namespace = splitted[0];
        return this;
    }

    Builder withRef(String ref) {
        this.ref = ref;
        return this;
    }

    private void mayBeCopy(boolean copy) throws IOException {
        if (copy)
            FileHelper.copyToBuildDir(repoPath, buildPath);
        else
            buildPath = repoPath;
    }
}
