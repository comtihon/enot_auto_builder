package com.coon.coon_auto_builder.config;

import com.coon.coon_auto_builder.service.build.Builder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ServerConfiguration {
    @Value("${default_erlang}")
    private String erlangVersion;

    @Value("${temp_path}")
    private String tempPath;

    @Value("${github_secret:null}")
    private String gitubSecret;

    public String getErlangVersion() {
        return erlangVersion;
    }

    public String getTempPath() {
        return tempPath;
    }

    public String getGitubSecret() {
        return gitubSecret;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    @Scope("prototype")
    @Lazy
    public Builder builder(Path repoPath, String erlang) {
        return new Builder(repoPath, erlang);
    }
}
