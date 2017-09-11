package com.coon.coon_auto_builder.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Entity
@Table(name = "package")
public class ErlPackage {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    @Column(name = "namespace", length = 100, nullable = false)
    private String namespace;
    @Column(name = "ref", length = 100, nullable = false)
    private String ref;
    @Column(name = "erl_vsn", length = 5, nullable = false)
    private String erlVsn;
    @Column(name = "path", length = 100, nullable = false)
    private String path;
    private String tempPath;

    public void init(String fullName, String ref, String erlVsn, String tempPath) {
        String[] splitted = fullName.split("/");
        this.namespace = splitted[0];
        this.name = splitted[1];
        this.ref = ref;
        this.erlVsn = erlVsn;
        this.tempPath = tempPath;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getRef() {
        return ref;
    }

    public String getId() {
        return id;
    }

    public String getErlVsn() {
        return erlVsn;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTempPath() {
        return tempPath;
    }

    public Path getSource() {
        return Paths.get(name, namespace, ref, erlVsn);
    }
}
