package com.coon.coon_auto_builder.data.entity;

import org.hibernate.annotations.GenericGenerator;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "builds")
public class Build {
    @Id
    @Column(name = "build_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String buildId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_version_id")
    private PackageVersion packageVersion;

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

    public Build() {
    }

    public Build(PackageVersion packageVersion, boolean result, String artifactPath) {
        this.packageVersion = packageVersion;
        this.result = result;
        this.artifactPath = artifactPath;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getBuildId() {
        return buildId;
    }

    public PackageVersion getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(PackageVersion packageVersion) {
        this.packageVersion = packageVersion;
    }

    public boolean isResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    @Override
    public String toString() {
        return "Build{" +
                "result=" + result +
                ", message='" + message + '\'' +
                '}';
    }
}
