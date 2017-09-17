package com.coon.coon_auto_builder.data.model;

import com.coon.coon_auto_builder.tool.CmdHelper;
import com.coon.coon_auto_builder.tool.FileHelper;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Table(name = "builds")
public class BuildBO {
    @Id
    @Column(name = "build_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String buildId;

    @ManyToOne(targetEntity = PackageVersionBO.class, cascade = CascadeType.ALL)
    @JoinColumn
    private PackageVersionBO packageVersion;

    @Column(name = "result", length = 100, nullable = false)
    private boolean result;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "artifact_path", length = 100)
    private Path artifactPath;

    @CreatedDate
    @NotNull
    @Column(name = "created_date", nullable = false, updatable = false)
    private ZonedDateTime createdDate = ZonedDateTime.now(); // TODO wrong format

    @Transient
    private Path basePath;
    @Transient
    private Path buildPath;

    BuildBO(PackageVersionBO packageVersion, Path basePath) {
        this.packageVersion = packageVersion;
        this.basePath = basePath;
        this.buildPath = Paths.get(basePath.toString(), packageVersion.getErlVsn());
    }

    BuildBO(PackageVersionBO packageVersion, String failMessage) {
        this.packageVersion = packageVersion;
        this.message = failMessage;
    }

    public boolean isSuccess() {
        return message == null;
    }

    public void setArtifactPath(Path artifactPath) {
        this.artifactPath = artifactPath;
    }

    public void setPackageVersion(PackageVersionBO packageVersion) {
        this.packageVersion = packageVersion;
    }

    public Path getBuildPath() {
        return buildPath;
    }

    public Path getArtifactPath() {
        return artifactPath;
    }

    void build(String erlangExecutable, boolean copy) {
        if (erlangExecutable == null) {
            message = "no erlang installed";
            return;
        }
        try {
            mayBeCopy(copy);
        } catch (IOException e) {
            message = "Can't copy from " + basePath + " to " + buildPath + ": " + e.getMessage();
            return;
        }
        ProcessBuilder pb = new ProcessBuilder("coon", "package");
        pb.directory(buildPath.toFile());
        Map<String, String> env = pb.environment();
        String path = env.get("PATH");
        env.put("PATH", Paths.get(erlangExecutable, "bin").toString() + ":" + path);
        try {
            Process process = pb.start();
            if (process.waitFor() != 0) {
                message = "build failed: " + CmdHelper.getProcessError(process);
            }
        } catch (IOException | InterruptedException e) {
            message = "build failed: " + e.getMessage();
        }
    }

    void clean() throws IOException {
        FileHelper.deleteDir(buildPath);
    }

    private void mayBeCopy(boolean copy) throws IOException {
        if (copy)
            FileHelper.copyToBuildDir(basePath, buildPath);
        else
            buildPath = basePath;
    }
}
