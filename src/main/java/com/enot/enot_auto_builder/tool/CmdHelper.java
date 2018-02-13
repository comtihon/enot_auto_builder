package com.enot.enot_auto_builder.tool;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
final public class CmdHelper {

    private CmdHelper() {

    }

    @NonNull
    public static String runCmd(String cmd) throws IOException, InterruptedException {
        log.debug("run {}", cmd);
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
