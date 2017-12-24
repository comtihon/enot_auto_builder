package com.coon.coon_auto_builder.data.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "builds")
@Data
public class Build {
    @Id
    @Column(name = "build_id")
    private String buildId = UUID.randomUUID().toString();

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
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate = new Date();

    public Build() {
    }

    public Build(boolean result, String artifactPath) {
        this(null, result, artifactPath);
    }

    public Build(String message) {
        this(null, false, "");
        this.message = message;
    }

    public Build(PackageVersion packageVersion, boolean result, String artifactPath) {
        this.packageVersion = packageVersion;
        this.result = result;
        this.artifactPath = artifactPath;
    }

    @Override
    public String toString() {
        return "Build{" +
                "id=" + buildId +
                ", result=" + result +
                ", message='" + message + '\'' +
                ", artifactPath='" + artifactPath + '\'' +
                '}';
    }
}
