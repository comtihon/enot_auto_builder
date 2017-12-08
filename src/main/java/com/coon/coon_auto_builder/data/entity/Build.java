package com.coon.coon_auto_builder.data.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "builds")
@Data
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
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate = new Date();

    public Build() {
    }

    public Build(boolean result, String artifactPath) {
        this(null, result, artifactPath);
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
                '}';
    }
}
