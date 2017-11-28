package com.coon.coon_auto_builder.data.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repository")
public class Repository {

    @Id
    @Column(name = "url", length = 100, nullable = false)
    private String url;
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    @Column(name = "namespace", length = 100, nullable = false)
    private String namespace;

    @OneToMany(mappedBy = "repository",
            cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PackageVersion> versions = new ArrayList<>();

    public Repository() {
    }

    public Repository(String url, String fullName, List<PackageVersion> versions) {
        this.url = url;
        String[] splitted = fullName.split("/");
        this.name = splitted[1];
        this.namespace = splitted[0];
        this.versions = versions;
        versions.forEach(version -> version.setRepository(this));
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public List<PackageVersion> getVersions() {
        return versions;
    }

    public void addVersion(PackageVersion version) {
        versions.add(version);
        version.setRepository(this);
    }

    @Override
    public String toString() {
        return "Repository{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                ", versions='" + versions + '\'' +
                '}';
    }
}
