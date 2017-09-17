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
    private String id;

    @ManyToOne(targetEntity = PackageVersionBO.class, cascade = CascadeType.ALL)
    @JoinColumn
    private RepositoryBO repository;

    @Column(name = "ref", length = 100, nullable = false)
    private String ref;

    @Column(name = "erl_vsn", length = 5, nullable = false)
    private String erlVsn;

    public PackageVersionBO() {
    }

    public PackageVersionBO(String ref, String erlVsn, RepositoryBO repository) {
        this.ref = ref;
        this.erlVsn = erlVsn;
        this.repository = repository;
    }

    public String getRef() {
        return ref;
    }

    public String getErlVsn() {
        return erlVsn;
    }

    public RepositoryBO getRepository() {
        return repository;
    }
}
