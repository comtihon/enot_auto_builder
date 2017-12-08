package com.coon.coon_auto_builder.data.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "repository")
@Data
@NoArgsConstructor
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
    private Set<PackageVersion> versions = new HashSet<>();

    public Repository(String url, String fullName, Set<PackageVersion> versions) {
        this.url = url;
        String[] splitted = fullName.split("/");
        this.name = splitted[1];
        this.namespace = splitted[0];
        this.versions = versions;
        versions.forEach(version -> version.setRepository(this));
    }

    public String getFullName() {
        return namespace + "/" + name;
    }

    public void addVersion(PackageVersion version) {
        version.setRepository(this);
        versions.add(version);
    }

    @Override
    public String toString() {
        return "Repository{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
