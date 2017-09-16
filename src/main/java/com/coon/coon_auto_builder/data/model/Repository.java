package com.coon.coon_auto_builder.data.model;

import com.coon.coon_auto_builder.data.dao.ErlPackage;
import com.coon.coon_auto_builder.system.ProcessException;
import com.coon.coon_auto_builder.tool.FileHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Repository {
    private Path repoPath; //Path where package is cloned
    private String url;
    private String ref;
    private String email;
    private String name;
    private String namespace;
    private List<String> erlangVersions;
    private Map<String, PackageBuilder> builders;

    Repository(String repoPath, String fullName, String ref, String url) {
        String[] splitted = fullName.split("/");
        this.namespace = splitted[0];
        this.name = splitted[1];
        this.repoPath = Paths.get(repoPath, fullName, ref);
        this.ref = ref;
        this.url = url;
        this.builders = new HashMap<>(); //TODO ConcurrentHashMap if read from other thread
    }

    public String getRef() {
        return ref;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getUrl() {
        return url;
    }

    public String getEmail() {
        return email;
    }

    public ErlPackage getErlPackage(List results) {
        return new ErlPackage(name, namespace, url, results);
    }

    public Map<String, PackageBuilder> getBuilders() {
        return builders;
    }

    public boolean isBuildSucceed() {
        for(PackageBuilder builder : builders.values())
            if (builder.isSuccess()) return true;
        return false;
    }

    boolean cloneRepo(String defaultErlang){
        if (!repoPath.toFile().mkdirs()) {
            String msg = "clone failed, can't create " + repoPath;
            builders.put(defaultErlang, new PackageBuilder(defaultErlang, msg));
            return false;
        }
        try (Git result = Git.cloneRepository()
                .setURI(url)
                .setDirectory(repoPath.toFile())
                .setBranch(ref)
                .call()) { //TODO change all println to log.debug
            System.out.println("Cloned " + url + " to " + result.getRepository().getDirectory());
            RevCommit commit = result.getRepository().parseCommit(result.getRepository().findRef(ref).getObjectId());
            email = commit.getAuthorIdent().getEmailAddress();
            readConfig(defaultErlang);
            return true;
        } catch (IOException | GitAPIException e) {
            String msg = "clone failed " + e.getMessage();
            builders.put(defaultErlang, new PackageBuilder(defaultErlang, msg));
            return false;
        }
    }

    void build(Map<String, String> erlangAvailable) {
        for (String erlang : erlangVersions) {
            PackageBuilder builder = new PackageBuilder(erlang, repoPath);
            builder.build(erlangAvailable.get(erlang), erlangVersions.size() > 1);
            builders.put(erlang, builder);
        }
    }

    void clean() throws IOException {
        for (PackageBuilder builder : builders.values()) {
            builder.clean();
        }
        FileHelper.deleteDir(repoPath);
    }

    private void readConfig(String defaultErlang) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map config = mapper.readValue(Paths.get(repoPath.toString(), "coonfig.json").toFile(), Map.class);
        erlangVersions = new ArrayList<>(Arrays.asList(parseErlangVsns(config, defaultErlang)));
    }

    private String[] parseErlangVsns(Map config, String defaultErlang) {
        if (config.containsKey("erlang")) {
            return (String[]) config.get("erlang");
        }
        return new String[]{defaultErlang};
    }
}
