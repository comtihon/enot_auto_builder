package com.coon.coon_auto_builder.config;

import com.coon.coon_auto_builder.service.build.Builder;
import com.coon.coon_auto_builder.tool.CmdHelper;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ServerConfiguration implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfiguration.class);
    @Value("${default_erlang}")
    private String erlangVersion;

    @Value("${kerl_executable}")
    private String kerlExecutable;

    @Value("${temp_path}")
    private String tempPath;

    @Value("${github_secret:null}")
    private String gitubSecret;

    private String kerlVersion;
    private String coonVersion;
    private Map<String, String> kerlInstallations = new ConcurrentHashMap<>();


    public String getErlangVersion() {
        return erlangVersion;
    }

    public Map<String, String> getKerlInstallations() {
        return kerlInstallations;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        checkKerlInstalled();
        gatherKerlInstallations();
        checkCoonInstalled();
        LOGGER.info("Coon version " + coonVersion);
        LOGGER.info("Kerl version " + kerlVersion);
        LOGGER.info("Kerl installations: ");
        for (Map.Entry<String, String> entry : kerlInstallations.entrySet())
            LOGGER.info(entry.getKey() + " " + entry.getValue());
    }

    private void checkCoonInstalled() {
        try {
            coonVersion = runCmd("coon -v");
        } catch (IOException | InterruptedException e) { //TODO do not fail if coon not found
            LOGGER.warn("Calling coon error " + e.getMessage());
            throw new RuntimeException("Calling coon error " + e.getMessage());
        }
    }

    private void checkKerlInstalled() {
        try {
            kerlVersion = runCmd(kerlExecutable + " version");
        } catch (IOException | InterruptedException e) { //TODO do not fail if kerl not found
            LOGGER.warn("Calling kerl error " + e.getMessage());
            throw new RuntimeException("Calling kerl error " + e.getMessage());
        }
    }

    private void gatherKerlInstallations() {
        String installations;
        try {
            installations = runCmd(kerlExecutable + " list installations");
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Calling kerl error");
            throw new RuntimeException("Calling kerl error");
        }
        String[] lines = installations.split("\n");
        for (String line : lines) {
            String[] installation = line.split(" ");
            kerlInstallations.put(trimKey(installation[0]), installation[1]);
        }
    }

    @NotNull
    private String runCmd(String cmd) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        return CmdHelper.getProcessOutput(p);
    }

    private String trimKey(String installation) {
        if (installation.contains(".")) {
            String[] splitted = installation.split("\\.");
            return splitted[0];
        } else {
            return installation;
        }
    }
}
