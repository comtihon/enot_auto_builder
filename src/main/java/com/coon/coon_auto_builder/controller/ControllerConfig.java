package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.data.model.BuildRequest;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class ControllerConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

    @Bean
    @Scope("prototype")
    @Lazy
    public BuildRequest buildRequest(RepositoryBO repo) {
        return new BuildRequest(repo);
    }
}
