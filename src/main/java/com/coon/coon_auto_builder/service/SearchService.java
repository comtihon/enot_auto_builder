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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SearchService extends AbstractService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BuildDAOService buildDao;

    @Async("searchExecutor")
    @Transactional
    public CompletableFuture<ResponseDTO<List<PackageDTO>>> searchPackages(
            String name, String namespace, String ref, String erlVsn) {
        log.debug("Search {}", name, namespace, ref, erlVsn);
        List<Build> builds = buildDao.findBy(name, namespace, ref, erlVsn, false);
        List<PackageDTO> packages = new ArrayList<>(builds.size());
        for (Build build : builds) {
            Repository repo = build.getPackageVersion().getRepository();
            String path;
            if (build.isResult())
                path = AbstractController.DOWNLOAD_ID.replace("{id}", build.getBuildId());
            else
                path = AbstractController.BUILD_LOG + "&build_id=" + build.getBuildId();
            PackageDTO packageDTO =
                    new PackageDTO(
                            build.getBuildId(),
                            repo.getNamespace(),
                            repo.getName(),
                            build.isResult(),
                            path,
                            build.getPackageVersion().getErlVersion(),
                            build.getPackageVersion().getRef(),
                            build.getCreatedDate());
            packages.add(packageDTO);
        }
        return CompletableFuture.completedFuture(ok(packages));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO<List<BuildDTO>>> fetchBuilds(RepositoryDTO request) {
        log.debug("Fetch {}", request);
        List<Build> builds = findBuilds(request);
        Type listType = new TypeToken<List<BuildDTO>>() {
        }.getType();
        List<BuildDTO> found = modelMapper.map(builds, listType);
        return CompletableFuture.completedFuture(ok(found));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO<List<PackageVersionDTO>>> searchVersions(RepositoryDTO request) {
        log.debug("Search {}", request);
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
        log.debug("Find {}", build_id);
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
            builds.addAll(buildDao.findBy(splitted[1], splitted[0]));
        } else {
            for (PackageVersionDTO pv : versions) {
                builds.addAll(buildDao.findBy(
                        splitted[1], splitted[0], pv.getRef(), pv.getErlVersion(), true));
            }
        }
        return builds;
    }
}
