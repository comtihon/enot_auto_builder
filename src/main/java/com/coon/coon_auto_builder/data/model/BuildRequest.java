package com.coon.coon_auto_builder.data.model;

import com.coon.coon_auto_builder.data.dto.BuildRequestDTO;
import com.coon.coon_auto_builder.loader.Loader;
import com.coon.coon_auto_builder.system.ServerConfiguration;
import com.coon.coon_auto_builder.system.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Transient;
import java.io.IOException;

public class BuildRequest implements Task {
    private Status internalStatus;
    private RepositoryBO repo;

    @Transient
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ServerConfiguration configuration;

    @Autowired
    private Loader loader;

    public BuildRequest() {

    }

    public BuildRequest(BuildRequestDTO dto) {
        internalStatus = Status.WAIT;
        repo = new RepositoryBO(configuration.getTempPath(), dto.getName(), dto.getRef(), dto.getUrl());
    }

    @Override
    public void process() {
        internalStatus = Status.CLONE;
        try {
            if (repo.cloneRepo(configuration.getErlangVersion())) {
                internalStatus = Status.BUILD;
                repo.build(configuration.getKerlInstallations());
                loader.loadArtifacts(repo);
                internalStatus = Status.FINISHED;
            } else { // just save build result with error message
                loader.loadArtifacts(repo);
            }
        } finally {
            try {
                repo.clean();
            } catch (IOException e) {
                logger.error("Failed to clean " + repo.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public MailReport generateEmail() {
        return new MailReport(repo.getEmail(),
                repo.getName(),
                internalStatus,
                repo.isBuildSucceed(),
                repo.getRef(),
                repo);
    }

    @Override
    public String key() {
        return repo.getNamespace() + "/" + repo.getName() + repo.getRef();
    }
}
