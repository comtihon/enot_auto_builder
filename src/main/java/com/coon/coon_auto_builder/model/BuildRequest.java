package com.coon.coon_auto_builder.model;

import com.coon.coon_auto_builder.controller.dto.BuildRequestDTO;
import com.coon.coon_auto_builder.domain.ErlPackage;
import com.coon.coon_auto_builder.loader.Loader;
import com.coon.coon_auto_builder.system.ProcessException;
import com.coon.coon_auto_builder.system.ServerConfiguration;
import com.coon.coon_auto_builder.system.Status;
import com.coon.coon_auto_builder.tool.CmdExec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BuildRequest implements Task {
    private String ref;
    private String refType;
    private String url;
    private String name;
    private List<String> erlangVersions;
    private Status status; //TODO status or results?
    private List<BuildResult> results = new ArrayList<>();
    private String email;
    private Path tempPath; //Path where package is cloned

    @Autowired
    private Loader loader;  //TODO loader is null (I don't know why).

    public void initFromDTO(BuildRequestDTO dto) {
        ref = dto.getRef();
        refType = dto.getRefType();
        url = dto.getUrl();
        name = dto.getName();
    }

    private String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    private void initFromRepo(String defaultErlang) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map config = mapper.readValue(Paths.get(tempPath.toString(), "coonfig.json").toFile(), Map.class);
        erlangVersions = new ArrayList<>(Arrays.asList(parseErlangVsns(config, defaultErlang)));
    }

    @Override
    public String toString() {
        return "BuildRequest{" +
                "ref='" + ref + '\'' +
                ", refType='" + refType + '\'' +
                ", url=" + url +
                ", name=" + name +
                '}';
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public Status getStatus() {
        return status;    //TODO should be thread safe
    }

    @Override
    public void process(ServerConfiguration configuration) throws ProcessException {
        tempPath = Paths.get(configuration.getTempPath(), getName(), ref);
        try {
            cloneRepo(configuration.getErlangVersion());
            status = Status.BUILD;
            Map<String, String> installations = configuration.getKerlInstallations();
            List<ErlPackage> artifacts = new ArrayList<>(erlangVersions.size());
            for (String erlang : erlangVersions) {
                try {
                    Path artifact = build(installations.get(erlang));
                    ErlPackage pack = new ErlPackage();
                    pack.init(getName(), ref, erlang, artifact.toString());
                    artifacts.add(pack);
                    addStep(true, "ok", erlang);
                } catch (Exception e) {
                    System.out.println(getName() + " " + erlang + " build failed");
                    System.out.println(e.getLocalizedMessage());
                    addStep(false, e.getMessage(), erlang);
                }
            }
            status = Status.LOAD;
            for (ErlPackage artifact : artifacts) {
                try {
                    loader.loadArtifact(artifact);
                } catch (IOException e) {
                    System.out.println(getName() + " load failed: " + e.getLocalizedMessage());
                    addStep(false, e.getMessage(), artifact.getErlVsn());
                }
            }
        } finally {
            for (String erlang : erlangVersions) {
                Path dirToDelete = getAnotherDirName(erlang);
                tryDelete(dirToDelete.toFile());
            }
            tryDelete(tempPath.toFile());
        }
    }

    @Override
    public void email() {

    }

    private void cloneRepo(String defaultErlang) throws ProcessException {
        status = Status.CLONE;
        try {
            doCloneRepo();
            initFromRepo(defaultErlang);
            addStep(true, "ok", null);
        } catch (IOException | GitAPIException e) {
            addStep(false, e.getLocalizedMessage(), null);
            throw new ProcessException("clone failed");
        }
    }

    private void doCloneRepo() throws IOException, GitAPIException {
        if (!tempPath.toFile().mkdirs()) {
            throw new IOException("Can't create " + tempPath);
        }
        try (Git result = Git.cloneRepository()
                .setURI(url)
                .setDirectory(tempPath.toFile())
                .setBranch(ref)
                .call()) { //TODO change to log.debug
            System.out.println("Cloned " + url + " to " + result.getRepository().getDirectory());
            RevCommit commit = result.getRepository().parseCommit(result.getRepository().findRef(ref).getObjectId());
            email = commit.getAuthorIdent().getEmailAddress();
        }
    }

    private void tryDelete(File dirToDelete) {
        System.out.println("delete " + dirToDelete);
        try {
            FileUtils.deleteDirectory(dirToDelete);
        } catch (IOException e) {
            System.out.println("Can't delete " + dirToDelete + ": " + e.getLocalizedMessage());
        }
    }

    private Path build(String erlang) throws ProcessException {
        if (erlang == null) {
            throw new ProcessException("no erlang installed");
        }
        System.out.println("build " + getName() + " with " + erlang);
        Path buildPath = copyToBuildDir(erlang);
        ProcessBuilder pb = new ProcessBuilder("coon", "package");
        pb.directory(buildPath.toFile());
        Map<String, String> env = pb.environment();
        String path = env.get("PATH");
        env.put("PATH", Paths.get(erlang, "bin").toString() + ":" + path);
        try {
            Process process = pb.start();
            if (process.waitFor() != 0) {
                throw new ProcessException("build failed: " + CmdExec.getProcessError(process));
            }
            return buildPath;
        } catch (IOException | InterruptedException e) {
            throw new ProcessException("build failed: " + e.getMessage());
        }
    }

    private Path getAnotherDirName(String erlang) {
        Path erl = Paths.get(erlang).getFileName();
        return Paths.get(tempPath.toString(), erl.toString());
    }

    //    Copy source to another dir before building to have originaly source untouched.
    private Path copyToBuildDir(String erlang) throws ProcessException {
        Path buildPath = getAnotherDirName(erlang);
        try {
            FileUtils.copyDirectory(tempPath.toFile(), buildPath.toFile());
        } catch (IOException e) {
            addStep(false, e.getLocalizedMessage(), erlang);
            throw new ProcessException("pre-build copy failed");
        }
        return buildPath;
    }

    // TODO add Status to steps to determine the state where the error comes from?
    private void addStep(boolean success, String message, String erlangVsn) {
        results.add(new BuildResult(name, ref, status, erlangVsn, success, message));
    }

    private String[] parseErlangVsns(Map config, String defaultErlang) {
        if (config.containsKey("erlang")) {
            return (String[]) config.get("erlang");
        }
        return new String[]{defaultErlang};
    }
}
