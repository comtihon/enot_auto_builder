package com.coon.coon_auto_builder.service.tool;

import com.coon.coon_auto_builder.config.ToolsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.coon.coon_auto_builder.tool.CmdHelper.runCmd;

public class Kerl implements Tool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsConfiguration.class);

    private final String kerlExecutable;
    private String kerlVersion;
    private Map<String, String> kerlInstallations = new ConcurrentHashMap<>();

    public Kerl(String kerlExecutable) {
        this.kerlExecutable = kerlExecutable;
    }

    @Override
    public boolean check() {
        try {
            kerlVersion = runCmd(kerlExecutable + " version");
            gatherKerlInstallations();
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Calling kerl error " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean install() {
        //TODO
        return false;
    }

    @Override
    public String version() {
        return kerlVersion;
    }

    public Map<String, String> getKerlInstallations() {
        return kerlInstallations;
    }

    private void gatherKerlInstallations() throws IOException, InterruptedException {
        String installations = runCmd(kerlExecutable + " list installations");
        String[] lines = installations.split("\n");
        for (String line : lines) {
            String[] installation = line.split(" ");
            kerlInstallations.put(trimKey(installation[0]), installation[1]);
        }
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
