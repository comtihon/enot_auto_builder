package com.coon.coon_auto_builder.tool;

import com.coon.coon_auto_builder.config.ToolsConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsConfiguration.class);

    @NotNull
    public static String runCmd(String cmd) throws IOException, InterruptedException {
        LOGGER.debug("run {}", cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        return getProcessOutput(p);
    }

    public static String getProcessOutput(Process process) throws IOException {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return getOutput(reader);
        }
    }

    public static String getProcessError(Process process) throws IOException {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            return getOutput(reader);
        }
    }

    private static String getOutput(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
