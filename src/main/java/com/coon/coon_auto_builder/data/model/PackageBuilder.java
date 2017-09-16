package com.coon.coon_auto_builder.data.model;

import com.coon.coon_auto_builder.data.dao.BuildResult;
import com.coon.coon_auto_builder.tool.CmdHelper;
import com.coon.coon_auto_builder.tool.FileHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class PackageBuilder {

    private String erlang;
    private Path basePath;
    private Path buildPath;
    private String failMessage = "OK";

    PackageBuilder(String erlang, Path basePath) {
        this.erlang = erlang;
        this.basePath = basePath;
        this.buildPath = Paths.get(basePath.toString(), erlang);
    }

    PackageBuilder(String erlang, String failMessage) {
        this.erlang = erlang;
        this.failMessage = failMessage;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public String getErlang() {
        return erlang;
    }

    public boolean isSuccess() {
        return failMessage.equals("OK");
    }

    public Path getBuildPath() {
        return buildPath;
    }

    public BuildResult getBuildResult(String path) {
        return new BuildResult(isSuccess(), failMessage, path, erlang);
    }

    void build(String erlangExecutable, boolean copy) {
        if (erlangExecutable == null) {
            failMessage = "no erlang installed";
            return;
        }
        try {
            mayBeCopy(copy);
        } catch (IOException e) {
            failMessage = "Can't copy from " + basePath + " to " + buildPath + ": " + e.getMessage();
            return;
        }
        ProcessBuilder pb = new ProcessBuilder("coon", "package");
        pb.directory(buildPath.toFile());
        Map<String, String> env = pb.environment();
        String path = env.get("PATH");
        env.put("PATH", Paths.get(erlangExecutable, "bin").toString() + ":" + path);
        try {
            Process process = pb.start();
            if (process.waitFor() != 0) {
                failMessage = "build failed: " + CmdHelper.getProcessError(process);
            }
        } catch (IOException | InterruptedException e) {
            failMessage = "build failed: " + e.getMessage();
        }
    }

    void clean() throws IOException {
        FileHelper.deleteDir(buildPath);
    }

    private void mayBeCopy(boolean copy) throws IOException {
        if (copy)
            FileHelper.copyToBuildDir(basePath, buildPath);
        else
            buildPath = basePath;
    }
}
