package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.controller.dto.PackageDTO;
import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class ErlPackageRestController extends AbstractController {

    @Autowired
    private SearchService searchService;
    @Value("${service.host}")
    private String serviceHost;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public CompletableFuture<ResponseEntity<?>> listPackagesBySearch(
            @RequestParam String name,
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String erl_version) {
        CompletableFuture<ResponseDTO<List<PackageDTO>>> packages = searchService.searchPackages(
                name, namespace, version, erl_version);
        return packages.thenApply(this::returnResult);
    }

    @RequestMapping(path = "/builds", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> listBuildsBySearch(
            @Valid @RequestBody RepositoryDTO request) {
        CompletableFuture<ResponseDTO<List<BuildDTO>>> builds = searchService.fetchBuilds(request);
        return builds.thenApply(this::returnResult);
    }

    @RequestMapping(path = "/versions", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> listVersionsBySearch(
            @Valid @RequestBody RepositoryDTO request) {
        CompletableFuture<ResponseDTO<List<PackageVersionDTO>>> builds = searchService.searchVersions(request);
        return builds.thenApply(this::returnResult);
    }

    /**
     * @param fullName git package namespace + nameEx. comtihon/coon
     * @return url  https://img.shields.io/badge/coon-<VSN>-green.svg?link=<Link-to-coonhub>
     */
    @RequestMapping(path = "/badge", method = RequestMethod.GET)
    public CompletableFuture<ResponseEntity> badgeForService(
            @RequestParam(name = "full_name") String fullName) {
        CompletableFuture<ResponseDTO> build = searchService.fetchLastSuccessfulVersion(fullName);
        return build.thenApply(responseDTO -> {
            if (responseDTO.isResult()) {
                PackageDTO data = (PackageDTO) responseDTO.getResponse();
                return new ResponseEntity<>(
                        "https://img.shields.io/badge/coon-" + data.getVersion()
                                + "-green.svg"
                                + "?link=" + serviceHost + data.getPath(),
                        HttpStatus.OK);
            }
            return new ResponseEntity<>("https://img.shields.io/badge/coon-unknown-red.svg", HttpStatus.OK);
        });
    }

    @RequestMapping(path = "/last_builds", method = RequestMethod.GET)
    public CompletableFuture<ResponseEntity<?>> listLastNBuilds(@RequestParam int n) {
        CompletableFuture<ResponseDTO<List<PackageDTO>>> builds = searchService.listBuilds(n);
        return builds.thenApply(this::returnResult);
    }

}
