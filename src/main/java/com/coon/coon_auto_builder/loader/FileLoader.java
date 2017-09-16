package com.coon.coon_auto_builder.loader;

import com.coon.coon_auto_builder.data.dao.BuildResult;
import com.coon.coon_auto_builder.data.dao.ErlPackage;
import com.coon.coon_auto_builder.data.dao.PackageVersion;
import com.coon.coon_auto_builder.data.dao.service.ErlPackageServiceInterface;
import com.coon.coon_auto_builder.data.dao.service.PackageVersionServiceInterface;
import com.coon.coon_auto_builder.data.model.PackageBuilder;
import com.coon.coon_auto_builder.data.model.Repository;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class FileLoader implements Loader {
    private final String artifactsPath;

    @Autowired
    private PackageVersionServiceInterface versionInterface;

    @Autowired
    private ErlPackageServiceInterface packageService;

    FileLoader(String path) {
        String[] splitted = path.split("file:");
        artifactsPath = splitted[1];
    }

    @Override
    public void loadArtifacts(Repository repository) {
        try {
            List<BuildResult> results = copyAndFormResults(repository);
            ErlPackage erlPackage = repository.getErlPackage(results);
            for (BuildResult result : results) {
                Optional<PackageVersion> mayBeVsn = versionInterface.findVersionByValues(
                        repository.getRef(), result.getErlang(), repository.getUrl());
                PackageVersion vsn = mayBeVsn.orElseGet(() ->
                        new PackageVersion(repository.getRef(), result.getErlang(), repository.getUrl()));
                result.setPackAndVsn(erlPackage, vsn);
            }
            packageService.savePackage(erlPackage);
        } catch (IOException e) {
            System.out.println(repository.getName() + " load failed: " + e.getMessage());
        }
    }

    private List<BuildResult> copyAndFormResults(Repository repository) throws IOException {
        Path src, dest;
        List<BuildResult> results = new ArrayList<>();
        for (Map.Entry<String, PackageBuilder> entry : repository.getBuilders().entrySet()) {
            String erlang = entry.getKey();
            PackageBuilder builder = entry.getValue();
            if (builder.isSuccess()) {
                dest = Paths.get(artifactsPath, repository.getName(),
                        repository.getNamespace(),
                        repository.getRef(),
                        erlang,
                        repository.getName() + ".cp");
                src = Paths.get(builder.getBuildPath().toString(), repository.getName() + ".cp");
                System.out.println("Copy " + src + " to " + dest);
                FileUtils.copyFile(src.toFile(), dest.toFile());
                results.add(builder.getBuildResult(dest.toString()));
            } else
                results.add(builder.getBuildResult("none"));
        }
        return results;
    }
}
