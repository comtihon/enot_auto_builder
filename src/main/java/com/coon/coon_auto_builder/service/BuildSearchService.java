package com.coon.coon_auto_builder.service;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.entity.Build;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class BuildSearchService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildSearchService.class);

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BuildDAOService buildDao;

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO<List<BuildDTO>>> fetchBuilds(RepositoryDTO request) {
        LOGGER.debug("Fetch {}", request);
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
        Type listType = new TypeToken<List<BuildDTO>>() {
        }.getType();
        List<BuildDTO> found = modelMapper.map(builds, listType);
        return CompletableFuture.completedFuture(ok(found));
    }

    @Async("searchExecutor")
    public CompletableFuture<ResponseDTO> findBuild(String build_id) {
        Optional<Build> find = buildDao.find(build_id);
        if (find.isPresent()) {
            BuildDTO dto = modelMapper.map(find.get(), BuildDTO.class);
            return CompletableFuture.completedFuture(ok(dto));
        }
        return CompletableFuture.completedFuture(fail("No build for id"));
    }
}
