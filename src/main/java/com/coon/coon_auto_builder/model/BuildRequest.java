package com.coon.coon_auto_builder.model;

import com.coon.coon_auto_builder.model.dto.BuildRequestDTO;
import com.coon.coon_auto_builder.domain.BuildResult;
import com.coon.coon_auto_builder.domain.ErlPackage;
import com.coon.coon_auto_builder.jpa.service.BuildResultServiceInterface;
import com.coon.coon_auto_builder.loader.Loader;
import com.coon.coon_auto_builder.system.MailSenderService;
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
import java.util.concurrent.CopyOnWriteArrayList;

public class BuildRequest implements Task {
    private final List<BuildResult> results = new CopyOnWriteArrayList<>();
    private Status internalStatus;
    private String ref;
    private String url;
    private String name;
    private List<String> erlangVersions;
    private String email;
    private Path tempPath; //Path where package is cloned

    @Autowired
    private ServerConfiguration configuration;

    @Autowired
    private Loader loader;

    @Autowired
    private MailSenderService mailSender;

    @Autowired
    private BuildResultServiceInterface buildResultService;

    public void initFromDTO(BuildRequestDTO dto) {
        ref = dto.getRef();
        url = dto.getUrl();
        name = dto.getName();
        internalStatus = Status.WAIT;
    }

    @Override
    public void process() throws ProcessException {
        tempPath = Paths.get(configuration.getTempPath(), name, ref);
        try {
            cloneRepo(configuration.getErlangVersion());
            Map<String, String> installations = configuration.getKerlInstallations();
            internalStatus = Status.BUILD;
            for (String erlang : erlangVersions) {
                try {
                    Path artifact = build(installations.get(erlang));
                    ErlPackage pack = new ErlPackage();
                    pack.init(name, ref, erlang, artifact.toString());
                    loader.loadArtifact(pack);
                    addFinishStep(erlang);
                } catch (IOException e) {
                    System.out.println(name + " load failed: " + e.getLocalizedMessage());
                    addStep(e.getMessage(), Status.LOAD, erlang);
                } catch (Exception e) {
                    System.out.println(name + " " + erlang + " build failed");
                    addStep(e.getMessage(), Status.BUILD, erlang);
                }
            }
            internalStatus = Status.FINISHED;
            sendEmail();
        } finally {
            for (String erlang : erlangVersions) {
                Path dirToDelete = getAnotherDirName(erlang);
                tryDelete(dirToDelete.toFile());
            }
            tryDelete(tempPath.toFile());
            dumpStatuses();
        }
    }

    @Override
    public void sendEmail() {
        mailSender.sendReport(email, name + ref, internalStatus, results);
    }

    @Override
    public String key() {
        return name + ref; //Ex. comtihon/bson1.0.0
    }

    private void dumpStatuses() {
        results.forEach(buildResultService::save);
    }

    private void cloneRepo(String defaultErlang) throws ProcessException {
        internalStatus = Status.CLONE;
        try {
            doCloneRepo();
            initFromRepo(defaultErlang);
        } catch (IOException | GitAPIException e) {
            addStep(e.getMessage(), Status.CLONE);
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
                .call()) { //TODO change all println to log.debug
            System.out.println("Cloned " + url + " to " + result.getRepository().getDirectory());
            RevCommit commit = result.getRepository().parseCommit(result.getRepository().findRef(ref).getObjectId());
            email = commit.getAuthorIdent().getEmailAddress();
        }
    }

    private void initFromRepo(String defaultErlang) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map config = mapper.readValue(Paths.get(tempPath.toString(), "coonfig.json").toFile(), Map.class);
        erlangVersions = new ArrayList<>(Arrays.asList(parseErlangVsns(config, defaultErlang)));
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
        System.out.println("build " + name + " with " + erlang);
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
            addStep(e.getMessage(), Status.CLONE, erlang);
            throw new ProcessException("pre-build copy failed");
        }
        return buildPath;
    }

    private String[] parseErlangVsns(Map config, String defaultErlang) {
        if (config.containsKey("erlang")) {
            return (String[]) config.get("erlang");
        }
        return new String[]{defaultErlang};
    }

    private void addFinishStep(String erlang) {
        results.add(new BuildStep()
                .withName(name)
                .withUrl(url)
                .withRef(ref)
                .withErlangVsn(erlang)
                .withStatus(Status.FINISHED).toResult());
    }

    private void addStep(String message, Status status) {
        addStep(message, status, null);
    }

    private void addStep(String message, Status status, String erlang) {
        results.add(new BuildStep()
                .withName(name)
                .withUrl(url)
                .withRef(ref)
                .withErlangVsn(erlang)
                .withMessage(message)
                .withResult(false)
                .withStatus(status).toResult());
    }
}
