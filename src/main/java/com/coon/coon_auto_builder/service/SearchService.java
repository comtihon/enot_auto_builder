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
        List<Build> builds = buildDao.findByGroupByPackage(name, namespace, ref, erlVsn, false);
        List<PackageDTO> packages = toPackages(builds);
        return CompletableFuture.completedFuture(ok(packages));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO<List<PackageDTO>>> listBuilds(int n) {
        log.debug("List {} builds", n);
        List<Build> builds = buildDao.getWithLimit(n);
        List<PackageDTO> packages = toPackages(builds);
        return CompletableFuture.completedFuture(ok(packages));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO> fetchBuild(RepositoryDTO request) {
        log.debug("Fetch {}", request);
        String[] splitted = request.getFullName().split("/");
        String ref = null;
        String erl = null;
        if (request.getVersions() != null && !request.getVersions().isEmpty()) {
            PackageVersionDTO versionDTO = request.getVersions().get(0);
            ref = versionDTO.getRef();
            erl = versionDTO.getErlVersion();
        }
        Optional<Build> found =
                buildDao.findBy(
                        splitted[1],
                        splitted[0],
                        ref,
                        erl);
        if (found.isPresent())
            return CompletableFuture.completedFuture(ok(modelMapper.map(found.get(), BuildDTO.class)));
        return CompletableFuture.completedFuture(fail("No such build"));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO<List<BuildDTO>>> fetchBuilds(RepositoryDTO request) {
        log.debug("Fetch {}", request);
        List<Build> builds = findBuilds(request, false); //TODO change DB request
        Type listType = new TypeToken<List<BuildDTO>>() {
        }.getType();
        List<BuildDTO> found = modelMapper.map(builds, listType);
        return CompletableFuture.completedFuture(ok(found));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO<List<PackageVersionDTO>>> searchVersions(RepositoryDTO request) {
        log.debug("Search {}", request);
        List<Build> builds = findBuilds(request, true);
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

    private List<Build> findBuilds(RepositoryDTO request, boolean onlySuccessfull) {
        String[] splitted = request.getFullName().split("/");
        List<PackageVersionDTO> versions = request.getVersions();
        List<Build> builds = new ArrayList<>();
        if (versions.isEmpty()) {
            builds.addAll(buildDao.findByGroupByPackage(
                    splitted[1], splitted[0], null, null, onlySuccessfull));
        } else {
            for (PackageVersionDTO pv : versions) {
                builds.addAll(buildDao.findByGroupByPackage(
                        splitted[1], splitted[0], pv.getRef(), pv.getErlVersion(), onlySuccessfull));
            }
        }
        return builds;
    }

    private List<PackageDTO> toPackages(List<Build> builds) {
        List<PackageDTO> packages = new ArrayList<>(builds.size());
        for (Build build : builds) {
            Repository repo = build.getPackageVersion().getRepository();
            String path;
            if (build.isResult())
                path = AbstractController.DOWNLOAD_ID.replace("{id}", build.getBuildId());
            else
                path = AbstractController.BUILD_LOG + "?build_id=" + build.getBuildId();
            PackageDTO packageDTO = PackageDTO.builder()
                    .buildId(build.getBuildId())
                    .namespace(repo.getNamespace())
                    .name(repo.getName())
                    .success(build.isResult())
                    .path(path)
                    .erlangVersion(build.getPackageVersion().getErlVersion())
                    .version(build.getPackageVersion().getRef())
                    .buildDate(build.getCreatedDate())
                    .build();
            packages.add(packageDTO);
        }
        return packages;
    }
}
