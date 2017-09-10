package com.coon.coon_auto_builder.loader;

import org.springframework.beans.factory.config.AbstractFactoryBean;

public class LoaderFactory extends AbstractFactoryBean<Loader> {

    private String path;
    private Class aClass;

    LoaderFactory(String path) {
        this.path = path;
        if (path.startsWith("file:")) {
            this.aClass = FileLoader.class;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return aClass;
    }

    @Override
    protected Loader createInstance() throws Exception {
        if (path.startsWith("file:")) {
            return new FileLoader(path);
        }
        return null;
    }
}
