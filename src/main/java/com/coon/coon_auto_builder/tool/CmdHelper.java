package com.coon.coon_auto_builder.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdHelper {

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
