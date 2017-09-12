package com.coon.coon_auto_builder.model.dto;

import com.coon.coon_auto_builder.domain.ErlPackage;

public class PackageDTO {
    private String id;
    private String namespace;
    private String name;
    private String ref;
    private String erl;

    public PackageDTO() {

    }

    public PackageDTO(ErlPackage pack) {
        this.namespace = pack.getNamespace();
        this.name = pack.getName();
        this.ref = pack.getRef();
        this.erl = pack.getErlVsn();
        this.id = pack.getId(); // TODO id is null?
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public String getErl() {
        return erl;
    }

    @Override
    public String toString() {
        return "{" +
                "namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", ref='" + ref + '\'' +
                ", erl='" + erl + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
