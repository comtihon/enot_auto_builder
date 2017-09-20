package com.coon.coon_auto_builder.system;

import com.coon.coon_auto_builder.data.model.BuildRequest;
import com.coon.coon_auto_builder.tool.CmdHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ServerConfiguration implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${default_erlang}")
    private String erlangVersion;

    @Value("${kerl_executable}")
    private String kerlExecutable;

    @Value("${temp_path}")
    private String tempPath;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        checkKerlInstalled();
        gatherKerlInstallations();
        checkCoonInstalled();
        logger.info("Coon version " + coonVersion);
        logger.info("Kerl version " + kerlVersion);
        logger.info("Kerl installations: ");
        for (Map.Entry<String, String> entry : kerlInstallations.entrySet())
            logger.info(entry.getKey() + " " + entry.getValue());
    }

    private void checkCoonInstalled() {
        try {
            coonVersion = runCmd("coon -v");
        } catch (IOException | InterruptedException e) { //TODO do not fail if coon not found
            logger.warn("Calling coon error " + e.getMessage());
            throw new RuntimeException("Calling coon error " + e.getMessage());
        }
    }

    private void checkKerlInstalled() {
        try {
            kerlVersion = runCmd(kerlExecutable + " version");
        } catch (IOException | InterruptedException e) { //TODO do not fail if kerl not found
            logger.warn("Calling kerl error " + e.getMessage());
            throw new RuntimeException("Calling kerl error " + e.getMessage());
        }
    }

    private void gatherKerlInstallations() {
        String installations;
        try {
            installations = runCmd(kerlExecutable + " list installations");
        } catch (IOException | InterruptedException e) {
            logger.error("Calling kerl error");
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
