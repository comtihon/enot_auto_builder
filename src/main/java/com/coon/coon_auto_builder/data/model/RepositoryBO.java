package com.coon.coon_auto_builder.data.model;

import com.coon.coon_auto_builder.tool.FileHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Entity
@Table(name = "repository")
public class RepositoryBO {
    @Transient
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Id
    @Column(name = "url", length = 100, nullable = false)
    private String url;
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    @Column(name = "namespace", length = 100, nullable = false)
    private String namespace;

    @Transient
    private Path repoPath; //Path where package is cloned
    @Transient
    private String ref;
    @Transient
    private String email;
    @Transient
    private List<String> erlangVersions;
    @Transient
    private Map<String, BuildBO> builders;

    public RepositoryBO() {

    }

    public RepositoryBO(String repoPath, String fullName, String ref, String url) {
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

    public Map<String, BuildBO> getBuilds() {
        return builders;
    }

    public boolean isBuildSucceed() {
        for (BuildBO builder : builders.values())
            if (builder.getResult()) return true;
        return false;
    }

    @Override
    public String toString() {
        return "RepositoryBO{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                ", repoPath=" + repoPath +
                ", ref='" + ref + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    boolean cloneRepo(String defaultErlang) {
        if (!repoPath.toFile().mkdirs()) {
            String msg = "clone failed, can't create " + repoPath;
            builders.put(defaultErlang, new BuildBO(new PackageVersionBO(ref, defaultErlang, this), msg));
            return false;
        }
        try (Git result = Git.cloneRepository()
                .setURI(url)
                .setDirectory(repoPath.toFile())
                .setBranch(ref)
                .call()) {
            logger.debug("Cloned " + url + " to " + result.getRepository().getDirectory());
            RevCommit commit = result.getRepository().parseCommit(result.getRepository().findRef(ref).getObjectId());
            email = commit.getAuthorIdent().getEmailAddress();
            readConfig(defaultErlang);
            return true;
        } catch (IOException | GitAPIException e) {
            String msg = "clone failed " + e.getMessage();
            logger.warn(msg);
            builders.put(defaultErlang, new BuildBO(new PackageVersionBO(ref, defaultErlang, this), msg));
            return false;
        }
    }

    void build(Map<String, String> erlangAvailable) {
        for (String erlang : erlangVersions) {
            BuildBO builder = new BuildBO(new PackageVersionBO(ref, erlang, this), repoPath);
            builder.build(erlangAvailable.get(erlang), erlangVersions.size() > 1);
            builders.put(erlang, builder);
        }
    }

    void clean() throws IOException {
        for (BuildBO builder : builders.values()) {
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
