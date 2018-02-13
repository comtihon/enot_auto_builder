package com.enot.enot_auto_builder.service.loader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoaderFactoryAppConfig {

    @Value("${artifacts_path}")
    private String artifactsPath;

    @Bean
    public LoaderFactory loaderFactory() {
        return new LoaderFactory(artifactsPath);
    }

    @Bean
    public Loader loader() throws Exception {
        return loaderFactory().getObject();
    }
}
