package com.enot.enot_auto_builder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Profile("non-async")
public class SyncConfig {
    @Bean("taskExecutor")
    public Executor asyncExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean("searchExecutor")
    public Executor asyncExecutor2() {
        return new SyncTaskExecutor();
    }
}
