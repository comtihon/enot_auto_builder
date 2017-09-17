package com.coon.coon_auto_builder.loader;

import com.coon.coon_auto_builder.data.dao.service.PackageVersionDAOService;
import com.coon.coon_auto_builder.data.dao.service.RepositoryDAOService;
import com.coon.coon_auto_builder.data.model.BuildBO;
import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;


public class FileLoader implements Loader {
    private final String artifactsPath;

    @Autowired
    PackageVersionDAOService pavkageVersionDAO;

    @Autowired
    RepositoryDAOService repositoryDAO;

    FileLoader(String path) {
        String[] splitted = path.split("file:");
        artifactsPath = splitted[1];
    }

    @Override
    @Nullable
    public RepositoryBO loadArtifacts(RepositoryBO repositoryBO) {
        try {
            copyArtifacts(repositoryBO);
            for (Map.Entry<String, BuildBO> result : repositoryBO.getBuilds().entrySet()) {
                final PackageVersionBO vsn = new PackageVersionBO(repositoryBO.getRef(), result.getKey(), repositoryBO);
                Optional<PackageVersionBO> mayBeVsn = pavkageVersionDAO.findByRefAndErlVersionAndRepositoryUrl(vsn);
                result.getValue().setPackageVersion(mayBeVsn.orElse(vsn));
            }
            return repositoryDAO.save(repositoryBO);
        } catch (IOException e) {
            System.out.println(repositoryBO.getName() + " load failed: " + e.getMessage());
            return null;
        }
    }

    private void copyArtifacts(RepositoryBO repositoryBO) throws IOException {
        Path src, dest;
        for (Map.Entry<String, BuildBO> entry : repositoryBO.getBuilds().entrySet()) {
            String erlang = entry.getKey();
            BuildBO builder = entry.getValue();
            if (builder.isSuccess()) {
                dest = Paths.get(artifactsPath, repositoryBO.getName(),
                        repositoryBO.getNamespace(),
                        repositoryBO.getRef(),
                        erlang,
                        repositoryBO.getName() + ".cp");
                src = Paths.get(builder.getBuildPath().toString(), repositoryBO.getName() + ".cp");
                System.out.println("Copy " + src + " to " + dest);
                FileUtils.copyFile(src.toFile(), dest.toFile());
                builder.setArtifactPath(dest);
            }
        }
    }
}
