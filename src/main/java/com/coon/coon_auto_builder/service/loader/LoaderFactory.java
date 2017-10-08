package com.coon.coon_auto_builder.service.loader;

import org.springframework.beans.factory.config.AbstractFactoryBean;

public class LoaderFactory extends AbstractFactoryBean<Loader> {

    private String path;
    private Class className;

    LoaderFactory(String path) {
        this.path = path;
        if (path.startsWith("file:")) {
            this.className = FileLoader.class;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return className;
    }

    @Override
    protected Loader createInstance() throws Exception {
        if (path.startsWith("file:")) {
            return new FileLoader(path);
        }
        return null;
    }
}
