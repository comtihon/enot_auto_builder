package com.coon.coon_auto_builder.data.dao;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "packages")
public class ErlPackage {
    @Id
    @Column(name = "url", length = 100, nullable = false)
    private String url;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "namespace", length = 100, nullable = false)
    private String namespace;

    @OneToMany(targetEntity = PackageVersion.class, cascade=CascadeType.ALL)
    private List versions;

    @OneToMany(targetEntity = BuildResult.class, cascade=CascadeType.ALL)
    private List builds;

    public ErlPackage() {

    }

    public ErlPackage(String name, String namespace, String url, List builds) {
        this.name = name;
        this.namespace = namespace;
        this.url = url;
        this.builds = builds;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getUrl() {
        return url;
    }
}
