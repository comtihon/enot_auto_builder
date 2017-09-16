package com.coon.coon_auto_builder.data.dao;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;

@Entity
@Table(name = "package_versions")
@Configurable(autowire = Autowire.BY_TYPE)
public class PackageVersion {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "pack_url", length = 100, nullable = false) //TODO join ErlPackage?
    private String packUrl;

    @Column(name = "ref", length = 100, nullable = false)
    private String ref;

    @Column(name = "erl_vsn", length = 5, nullable = false)
    private String erlVsn;

    public PackageVersion() {
    }

    public PackageVersion(String ref, String erlVsn, String packUrl) {
        this.ref = ref;
        this.erlVsn = erlVsn;
        this.packUrl = packUrl;
    }

    public String getRef() {
        return ref;
    }

    public String getErlVsn() {
        return erlVsn;
    }

    public String getPackUrl() {
        return packUrl;
    }
}
