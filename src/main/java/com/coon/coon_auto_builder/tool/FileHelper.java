package com.coon.coon_auto_builder.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
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
     * @param repoPath path to the cloned repo
     * @return erlangs from configuration
     * @throws IOException in case of configuration absence.
     */
    public static Map<String, Object> readConfig(Path repoPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File config = Paths.get(repoPath.toString(), "coonfig.json").toFile();
        return mapper.readValue(config, Map.class);
    }

    public static List<String> parseErlangVsns(Map config, String defaultErlang) {
        String[] erlangs;
        if (config.containsKey("erlang")) {
            return (List<String>) config.get("erlang");
        } else
            erlangs = new String[]{defaultErlang};
        return new ArrayList<>(Arrays.asList(erlangs));
    }

    public static String parseName(Map config) {
        if (config.containsKey("name")) {
            return (String) config.get("name");
        }
        return null;
    }

}
