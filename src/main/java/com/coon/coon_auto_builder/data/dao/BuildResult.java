package com.coon.coon_auto_builder.data.dao;

import org.eclipse.jgit.annotations.Nullable;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Entity
@Table(name = "build_results")
public class BuildResult {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne(targetEntity = ErlPackage.class)
    @JoinColumn
    private ErlPackage pack;

    @ManyToOne(targetEntity = PackageVersion.class, cascade=CascadeType.ALL)
    @JoinColumn
    private PackageVersion version;

    @Column(name = "result", length = 100, nullable = false)
    private boolean result;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "path", length = 100)
    private String path;

    @CreatedDate
    @NotNull
    @Column(name = "created_date", nullable = false, updatable = false)
    private ZonedDateTime createdDate = ZonedDateTime.now(); // TODO wrong format

    private String erlang;

    public BuildResult() {

    }

    public BuildResult(boolean result, String message, String path, String erlang) {
        this.result = result;
        this.message = message;
        this.path = path;
        this.erlang = erlang;
    }

    public void setPackAndVsn(ErlPackage pack, PackageVersion version) {
        this.pack = pack;
        this.version = version;
    }

    @Nullable
    public String getErlang() {
        return erlang;
    }
}
