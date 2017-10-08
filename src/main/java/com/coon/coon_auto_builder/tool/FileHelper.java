package com.coon.coon_auto_builder.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileHelper {

    public static void copyToBuildDir(Path src, Path dst) throws IOException {
        FileUtils.copyDirectory(src.toFile(), dst.toFile());
    }

    public static void deleteDir(Path dir) throws IOException {
        FileUtils.deleteDirectory(dir.toFile());
    }

    /**
     * Get list of erlang versions from coonfig.json
     *
     * @param repoPath      path to the cloned repo
     * @param defaultErlang default erlang
     * @return erlangs from configuration
     * @throws IOException in case of configuration absence.
     */
    public static List<String> readConfig(Path repoPath, String defaultErlang) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map config = mapper.readValue(Paths.get(repoPath.toString(), "coonfig.json").toFile(), Map.class);
        return new ArrayList<>(Arrays.asList(parseErlangVsns(config, defaultErlang)));
    }

    private static String[] parseErlangVsns(Map config, String defaultErlang) {
        if (config.containsKey("erlang")) {
            return (String[]) config.get("erlang");
        }
        return new String[]{defaultErlang};
    }

}
