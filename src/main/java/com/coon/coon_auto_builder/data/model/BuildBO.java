package com.coon.coon_auto_builder.data.model;

import com.coon.coon_auto_builder.data.dto.PackageDTO;
import com.coon.coon_auto_builder.tool.CmdHelper;
import com.coon.coon_auto_builder.tool.FileHelper;
import org.hibernate.annotations.GenericGenerator;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "builds")
public class BuildBO {
    @Id
    @Column(name = "build_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String buildId;

    @ManyToOne(targetEntity = PackageVersionBO.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "package_version_id")
    private PackageVersionBO packageVersion;

    @Column(name = "result", length = 100, nullable = false)
    private boolean result = true;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "artifact_path", length = 100)
    private String artifactPath;

    @CreatedDate
    @NotNull
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate = new Date();

    @Transient
    private Path basePath;
    @Transient
    private Path buildPath;

    public BuildBO() {
    }

    public BuildBO(PackageVersionBO packageVersion, Path basePath) {
        this.packageVersion = packageVersion;
        this.basePath = basePath;
        this.buildPath = Paths.get(basePath.toString(), packageVersion.getErlVersion());
    }

    public BuildBO(PackageVersionBO packageVersion, String failMessage) {
        this.packageVersion = packageVersion;
        this.message = failMessage;
        if (failMessage != null)
            this.result = false;
    }

    public boolean getResult() {
        return result;
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    @NotNull
    public Date getCreatedDate() {
        return createdDate;
    }

    public PackageVersionBO getPackageVersion() {
        return packageVersion;
    }

    public Path getBuildPath() {
        return buildPath;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(Path artifactPath) {
        this.artifactPath = artifactPath.toString();
    }

    public String getMessage() {
        return message;
    }

    public PackageDTO toDTO() {
        return new PackageDTO(packageVersion);
    }

    @Override
    public String toString() {
        return "BuildBO{" +
                "buildId='" + buildId + '\'' +
                ", packageVersion=" + packageVersion +
                ", result=" + result +
                ", message='" + message + '\'' +
                ", artifactPath='" + artifactPath + '\'' +
                ", createdDate=" + createdDate +
                ", basePath=" + basePath +
                ", buildPath=" + buildPath +
                '}';
    }

    void build(String erlangExecutable, boolean copy) {
        if (erlangExecutable == null) {
            message = "no erlang installed";
            result = false;
            return;
        }
        try {
            mayBeCopy(copy);
        } catch (IOException e) {
            message = "Can't copy from " + basePath + " to " + buildPath + ": " + e.getMessage();
            result = false;
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
                result = false;
            }
        } catch (IOException | InterruptedException e) {
            message = "build failed: " + e.getMessage();
            result = false;
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
