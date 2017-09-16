package com.coon.coon_auto_builder.tool;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

public class FileHelper {

    public static void copyToBuildDir(Path src, Path dst) throws IOException {
        FileUtils.copyDirectory(src.toFile(), dst.toFile());
    }

    public static void deleteDir(Path dir) throws IOException {
        FileUtils.deleteDirectory(dir.toFile());
    }

}
