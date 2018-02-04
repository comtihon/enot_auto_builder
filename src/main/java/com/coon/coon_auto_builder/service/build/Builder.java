package com.coon.coon_auto_builder.service.build;

import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.PackageVersion;
import com.coon.coon_auto_builder.service.Metrics;
import com.coon.coon_auto_builder.service.git.ClonedRepo;
import com.coon.coon_auto_builder.service.tool.Coon;
import com.coon.coon_auto_builder.service.tool.Erlang;
import com.coon.coon_auto_builder.service.tool.Kerl;
import com.coon.coon_auto_builder.tool.ErlangHelper;
import com.coon.coon_auto_builder.tool.FileHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Builder {
    private final ClonedRepo repo;
    @Getter
    @Setter
    private Path buildPath;
    @Getter
    private String erlang;
    @Getter
    private String ref;
    @Getter
    private String name;
    @Getter
    private String namespace;
    // Name of the coon package. Should be based on application name from .app file
    @Getter
    private String packageName;

    @Autowired
    private Kerl kerl;

    @Autowired
    private Coon coon;

    @Autowired
    private GaugeService gaugeService;

    public Builder(ClonedRepo repo, String erlang) {
        this.repo = repo;
        this.erlang = erlang;
    }

    void buildVersion(boolean copy) throws BuildException {
        Map<String, Erlang> compilers = kerl.getErlInstallations();
        String erlangExecutable = compilers.get(erlang).getPath();
        if (erlangExecutable == null) {
            this.gaugeService.submit(Metrics.BUILD_FAIL.toString(), 1.0);
            throw new BuildException("no erlang installed");
        }
        try {
            mayBeCopy(copy);
        } catch (IOException e) {
            this.gaugeService.submit(Metrics.BUILD_FAIL.toString(), 1.0);
            throw new BuildException("Can't copy from " + repo + " to " + buildPath + ": " + e.getMessage());
        }
        try {
            coon.build(buildPath, erlangExecutable);
            this.gaugeService.submit(Metrics.BUILD_OK.toString(), 1.0);
        } catch (IOException | InterruptedException | BuildException e) {
            this.gaugeService.submit(Metrics.BUILD_FAIL.toString(), 1.0);
            throw new BuildException("build failed: " + e.getMessage());
        }
    }

    Build getBuild(String artifactPath, String message) {
        PackageVersion version = new PackageVersion(ref, erlang);
        Build build = new Build(version, artifactPath != null, artifactPath);
        build.setMessage(message);
        return build;
    }

    void detectPackageName(Map<String, Object> projectConf) {
        if (projectConf.isEmpty()) // can be empty if not used in formErlangForVersions
            try {
                projectConf.putAll(repo.getConfig());
            } catch (IOException ignored) {
            }
        String name = FileHelper.parseName(projectConf);
        if (name == null || name.isEmpty()) {
            // no name specified in project config. try to find .app manually
            Path appConf = getAppConfPath();
            if (appConf != null)
                name = ErlangHelper.getApplicationName(appConf);
        }
        if (name == null)
            name = getName();
        packageName = name;
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

    /**
     * @return path to Erlang application configuration file. Usually ebin/Project.app
     */
    private Path getAppConfPath() {
        List<Path> appConfigs;
        try {
            appConfigs = Files.walk(Paths.get(buildPath.toString(), "ebin"))
                    .filter(file -> file.getFileName().toString().endsWith(".app"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("No ebin dir found in {} for {}!", buildPath, name);
            return null;
        }
        if (appConfigs.size() == 0) {
            log.warn("No .app file found for {}!", name);
            return null;
        }
        if (appConfigs.size() > 1) {
            log.warn("More than one .app file for {}!", name);
        }
        return appConfigs.get(0);
    }

    /**
     * In case of builder need to build only one erlang version - it will be built
     * in the current (cloned) directory.
     * In case of several versions - cloned directory will be copied to /ErlVsn/ subdir
     * before build.
     *
     * @param copy need to copy
     * @throws IOException
     */
    private void mayBeCopy(boolean copy) throws IOException {
        if (copy) {
            buildPath = Paths.get(repo.toString(), erlang);
            FileHelper.copyToBuildDir(repo.getCloned(), buildPath);
        } else
            buildPath = repo.getCloned();
    }
}
