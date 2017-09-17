package com.coon.coon_auto_builder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class CoonAutoBuilderApplication {


    public static void main(String[] args) {
        SpringApplication.run(CoonAutoBuilderApplication.class, args);
    }

    @Bean("taskExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(cores * 2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("BuildBO-");
        executor.initialize();
        return executor;
    }
}
