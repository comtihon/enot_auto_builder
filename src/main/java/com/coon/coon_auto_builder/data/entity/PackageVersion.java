package com.coon.coon_auto_builder.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "package_versions")
@Configurable(autowire = Autowire.BY_TYPE)  //TODO do I need this?
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"versionId"})
public class PackageVersion {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "version_id")
    private String versionId;

    @Column(name = "ref", length = 100, nullable = false)
    private String ref;

    @Column(name = "erl_version", length = 10, nullable = false)
    private String erlVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_url")
    private Repository repository;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "packageVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Build> buildsRes = new ArrayList<>();

    @Transient
    @Nullable
    private String email; //email of last commit for this ref.

    public PackageVersion(String ref, String erlVersion) {
        this.ref = ref;
        this.erlVersion = erlVersion;
    }

    public void addBuild(Build build) {
        build.setPackageVersion(this);
        buildsRes.add(build);
    }

    @Override
    public String toString() {
        return "PackageVersion{" +
                "id='" + versionId + '\'' +
                ", ref='" + ref + '\'' +
                ", erlVersion='" + erlVersion + '\'' +
                '}';
    }
}
