package com.enot.enot_auto_builder.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

final public class FileHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);

    private FileHelper() {

    }

    public static void copyToBuildDir(Path src, Path dst) throws IOException {
        FileUtils.copyDirectory(src.toFile(), dst.toFile());
    }

    public static void deleteDir(Path dir) throws IOException {
        FileUtils.deleteDirectory(dir.toFile());
    }

    /**
     * Get list of erlang versions from enot_config.json
     *
     * @param repoPath path to the cloned repo
     * @return erlangs from configuration
     * @throws IOException in case of configuration absence.
     */
    public static Map<String, Object> readConfig(Path repoPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File config = Paths.get(repoPath.toString(), "enot_config.json").toFile();
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

    public static void compress(Path input, String output) throws IOException {
        try (TarArchiveOutputStream out = getTarArchiveOutputStream(output)) {
            addToArchiveCompression(out, input.toFile(), ".");
        }
    }

    private static TarArchiveOutputStream getTarArchiveOutputStream(String name) throws IOException {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(new FileOutputStream(name));
        // TAR has an 8 gig file limit by default, this gets around that
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        // TAR originally didn't support long file names, so enable the support for it
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        taos.setAddPaxHeadersForNonAsciiNames(true);
        return taos;
    }

    private static void addToArchiveCompression(TarArchiveOutputStream out, File file, String dir) throws IOException {
        String entry = dir + File.separator + file.getName();
        if (file.isFile()) {
            out.putArchiveEntry(new TarArchiveEntry(file, entry));
            try (FileInputStream in = new FileInputStream(file)) {
                IOUtils.copy(in, out);
            }
            out.closeArchiveEntry();
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addToArchiveCompression(out, child, entry);
                }
            }
        } else {
            LOGGER.error("{} is not supported", file.getName());
        }
    }
}
