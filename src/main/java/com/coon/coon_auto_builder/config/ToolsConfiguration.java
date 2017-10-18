package com.coon.coon_auto_builder.config;

import com.coon.coon_auto_builder.service.tool.Coon;
import com.coon.coon_auto_builder.service.tool.Kerl;
import com.coon.coon_auto_builder.service.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ToolsConfiguration implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsConfiguration.class);

    @Value("${kerl_executable}")
    private String kerlExecutable;

    @Override
    public void afterPropertiesSet() throws Exception {
        Coon coon = coon();
        Kerl kerl = kerl();
        List<Tool> tools = Arrays.asList(coon, kerl);
        tools.forEach(tool -> {
            tool.check();
            if (tool.isReady())
                LOGGER.info("{}", tool);
            else
                LOGGER.info(tool.getClass().getName() + " not ready: " + tool.getMessage());
        });
    }

    @Bean
    @Lazy
    public Coon coon() {
        return new Coon();
    }

    @Bean
    @Lazy
    public Kerl kerl() {
        return new Kerl(kerlExecutable);
    }
}
