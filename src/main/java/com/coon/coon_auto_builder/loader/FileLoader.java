package com.coon.coon_auto_builder.loader;

import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dao.PackageVersionDAOService;
import com.coon.coon_auto_builder.data.model.BuildBO;
import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;


public class FileLoader implements Loader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String artifactsPath;

    @Autowired
    BuildDAOService buildDAO;


    FileLoader(String path) {
        String[] splitted = path.split("file:");
        artifactsPath = splitted[1];
    }

    @Override
    public void loadArtifacts(RepositoryBO repositoryBO) {
        try {
            copyArtifacts(repositoryBO);
            for (Map.Entry<String, BuildBO> result : repositoryBO.getBuilds().entrySet()) {
                BuildBO build = result.getValue();
                buildDAO.save(build);
            }
        } catch (IOException e) {
            logger.error(repositoryBO.getName() + " load failed: " + e.getMessage());
        }
    }

    private void copyArtifacts(RepositoryBO repositoryBO) throws IOException {
        Path src, dest;
        for (Map.Entry<String, BuildBO> entry : repositoryBO.getBuilds().entrySet()) {
            String erlang = entry.getKey();
            BuildBO builder = entry.getValue();
            if (builder.getResult()) {
                dest = Paths.get(artifactsPath, repositoryBO.getName(),
                        repositoryBO.getNamespace(),
                        repositoryBO.getRef(),
                        erlang,
                        repositoryBO.getName() + ".cp");
                src = Paths.get(builder.getBuildPath().toString(), repositoryBO.getName() + ".cp");
                logger.debug("Copy " + src + " to " + dest);
                FileUtils.copyFile(src.toFile(), dest.toFile());
                builder.setArtifactPath(dest);
            }
        }
    }
}
