package com.coon.coon_auto_builder.data.model;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;

@Entity
@Table(name = "package_versions")
@Configurable(autowire = Autowire.BY_TYPE)
public class PackageVersionBO {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "version_id")
    private String versionId;

    @ManyToOne(targetEntity = RepositoryBO.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_url")
    private RepositoryBO repository;

    @Column(name = "ref", length = 100, nullable = false)
    private String ref;

    @Column(name = "erl_version", length = 5, nullable = false)
    private String erlVersion;

    public PackageVersionBO() {
    }

    public PackageVersionBO(String ref, String erlVersion, RepositoryBO repository) {
        this.ref = ref;
        this.erlVersion = erlVersion;
        this.repository = repository;
    }

    @Override
    public String toString() {
        return "PackageVersionBO{" +
                "versionId='" + versionId + '\'' +
                ", repository=" + repository +
                ", ref='" + ref + '\'' +
                ", erlVersion='" + erlVersion + '\'' +
                '}';
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getRef() {
        return ref;
    }

    public String getErlVersion() {
        return erlVersion;
    }

    public RepositoryBO getRepository() {
        return repository;
    }

    public void setRepository(RepositoryBO repository) {
        this.repository = repository;
    }
}
