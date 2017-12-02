package com.coon.coon_auto_builder.service;

import com.coon.coon_auto_builder.controller.AbstractController;
import com.coon.coon_auto_builder.controller.dto.PackageDTO;
import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.PackageVersion;
import com.coon.coon_auto_builder.data.entity.Repository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class SearchService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BuildDAOService buildDao;

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO<List<PackageDTO>>> searchPackages(
            String name, String namespace, String ref, String erlVsn) {
        LOGGER.debug("Search {}", name, namespace, ref, erlVsn);
        List<Build> builds = buildDao.findAllByValues(name, namespace, ref, erlVsn);
        List<PackageDTO> packages = new ArrayList<>(builds.size());
        for (Build build : builds) {
            Repository repo = build.getPackageVersion().getRepository();
            PackageDTO packageDTO = new PackageDTO(build.getBuildId(), repo.getName(), repo.getNamespace());
            packageDTO.setBuildDate(build.getCreatedDate());
            packageDTO.setSuccess(build.isResult());
            if (build.isResult()) {
                packageDTO.setPath(AbstractController.DOWNLOAD_ID + "/" + build.getBuildId());
            } else {
                packageDTO.setPath(AbstractController.BUILD_LOG + "&build_id=" + build.getBuildId());
            }
            packages.add(packageDTO);
        }
        //TODO remove test data
        PackageDTO ok = new PackageDTO();
        ok.setBuildId("1");
        ok.setSuccess(true);
        ok.setName("test");
        ok.setNamespace("comtihon");
        ok.setPath("/path/to/artifact.cp");
        ok.setBuildDate(new Date());
        PackageDTO fail = new PackageDTO();
        fail.setBuildId("2");
        fail.setSuccess(false);
        fail.setName("other");
        fail.setNamespace("comtihon");
        fail.setPath("/path/to/log");
        fail.setBuildDate(new Date());
        packages.add(ok);
        packages.add(fail);
        return CompletableFuture.completedFuture(ok(packages));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO<List<BuildDTO>>> fetchBuilds(RepositoryDTO request) {
        LOGGER.debug("Fetch {}", request);
        List<Build> builds = findBuilds(request);
        Type listType = new TypeToken<List<BuildDTO>>() {
        }.getType();
        List<BuildDTO> found = modelMapper.map(builds, listType);
        return CompletableFuture.completedFuture(ok(found));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO<List<PackageVersionDTO>>> searchVersions(RepositoryDTO request) {
        LOGGER.debug("Search {}", request);
        List<Build> builds = findBuilds(request);
        Set<PackageVersion> versions = new HashSet<>();
        for (Build b : builds)
            versions.add(b.getPackageVersion());
        Type listType = new TypeToken<List<PackageVersionDTO>>() {
        }.getType();
        List<PackageVersionDTO> found = modelMapper.map(versions, listType);
        return CompletableFuture.completedFuture(ok(found));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO> findBuild(String build_id) {
        LOGGER.debug("Find {}", build_id);
        Optional<Build> find = buildDao.find(build_id);
        if (find.isPresent()) {
            BuildDTO dto = modelMapper.map(find.get(), BuildDTO.class);
            return CompletableFuture.completedFuture(ok(dto));
        }
        return CompletableFuture.completedFuture(fail("No build for id"));
    }

    private List<Build> findBuilds(RepositoryDTO request) {
        String[] splitted = request.getFullName().split("/");
        List<PackageVersionDTO> versions = request.getVersions();
        List<Build> builds = new ArrayList<>();
        if (versions.isEmpty()) {
            builds.addAll(buildDao.fetchByValues(splitted[1], splitted[0]));
        } else {
            for (PackageVersionDTO pv : versions) {
                builds.addAll(buildDao.fetchByValues(
                        splitted[1], splitted[0], pv.getRef(), pv.getErlVersion()));
            }
        }
        return builds;
    }
}
