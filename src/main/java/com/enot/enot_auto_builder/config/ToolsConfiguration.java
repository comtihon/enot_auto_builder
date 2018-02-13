package com.enot.enot_auto_builder.config;

import com.enot.enot_auto_builder.service.tool.Enot;
import com.enot.enot_auto_builder.service.tool.Kerl;
import com.enot.enot_auto_builder.service.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Slf4j
public class ToolsConfiguration implements InitializingBean {

    @Value("${kerl_executable}")
    private String kerlExecutable;

    @Override
    public void afterPropertiesSet() throws Exception {
        Enot enot = enot();
        Kerl kerl = kerl();
        List<Tool> tools = Arrays.asList(enot, kerl);
        tools.forEach(tool -> {
            tool.check();
            if (tool.isReady())
                log.info("{}", tool);
            else
                log.info(tool.getClass().getSimpleName() + " not ready: " + tool.getMessage());
        });
    }

    @Bean
    @Lazy
    public Enot enot() {
        return new Enot();
    }

    @Bean
    @Lazy
    public Kerl kerl() {
        return new Kerl(kerlExecutable);
    }
}
