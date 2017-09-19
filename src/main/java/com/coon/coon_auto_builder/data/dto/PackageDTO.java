package com.coon.coon_auto_builder.data.dto;

import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import com.coon.coon_auto_builder.data.model.RepositoryBO;

public class PackageDTO {
    private String url;
    private String namespace;
    private String name;
    private String ref;
    private String erl;

    public PackageDTO() {

    }

    public PackageDTO(PackageVersionBO version) {
        this.namespace = version.getRepository().getNamespace();
        this.name = version.getRepository().getName();
        this.ref = version.getRef();
        this.erl = version.getErlVersion();
        this.url = version.getRepository().getUrl();
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
                ", url='" + url + '\'' +
                '}';
    }
}
