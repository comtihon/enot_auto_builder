package com.coon.coon_auto_builder.service.tool;

import com.coon.coon_auto_builder.config.ToolsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.coon.coon_auto_builder.tool.CmdHelper.runCmd;

public class Kerl extends Tool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsConfiguration.class);

    private final String kerlExecutable;
    private Map<String, String> erlInstallations = new ConcurrentHashMap<>();

    public Kerl(String kerlExecutable) {
        this.kerlExecutable = kerlExecutable;
    }

    @Override
    public boolean check() {
        try {
            version = runCmd(kerlExecutable + " version").trim();
            gatherKerlInstallations();
            ready = true;
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Calling kerl error {}", e.getMessage());
            message = e.getMessage();
            return false;
        }
    }

    @Override
    public boolean install() {
        //TODO
        return false;
    }

    public Map<String, String> getErlInstallations() {
        return erlInstallations;
    }

    @Override
    public String toString() {
        StringBuilder installations = new StringBuilder();
        for (Map.Entry<String, String> entry : erlInstallations.entrySet())
            installations.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
        return "Kerl version='" + version + "'" +
                ", erlInstallations:\n" + installations.toString();
    }

    private void gatherKerlInstallations() throws IOException, InterruptedException {
        String installations = runCmd(kerlExecutable + " list installations");
        String[] lines = installations.split("\n");
        for (String line : lines) {
            String[] installation = line.split(" ");
            erlInstallations.put(trimKey(installation[0]), installation[1]);
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
