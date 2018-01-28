package com.coon.coon_auto_builder.data.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static com.coon.coon_auto_builder.tool.UrlHelper.removeGitEnding;

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
        this.url = removeGitEnding(url);
        this.versions = versions;
        versions.forEach(version -> version.setRepository(this));
        setName(fullName);
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

    private void setName(String fullName) {
        String[] splitted = fullName.split("/");
        if (splitted.length > 1) {
            this.name = splitted[1];
            this.namespace = splitted[0];
        } else {
            splitted = this.url.split("/");
            this.name = splitted[splitted.length - 1];
            this.namespace = splitted[splitted.length - 2];
        }
    }
}
