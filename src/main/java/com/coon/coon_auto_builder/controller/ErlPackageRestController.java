package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.controller.dto.PackageDTO;
import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class ErlPackageRestController extends AbstractController {

    @Autowired
    private SearchService searchService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public CompletableFuture<ResponseEntity<?>> listPackagesBySearch(
            @RequestParam String name,
            @RequestParam String namespace,
            @RequestParam String version,
            @RequestParam String erl_version) {
        CompletableFuture<ResponseDTO<List<PackageDTO>>> packages = searchService.searchPackages(
                name, namespace, version, erl_version);
        return packages.thenApply(this::returnResult);
    }

    @RequestMapping(path = "/builds", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> listBuildsBySearch(
            @Valid @RequestBody RepositoryDTO request) throws IOException {
        CompletableFuture<ResponseDTO<List<BuildDTO>>> builds = searchService.fetchBuilds(request);
        return builds.thenApply(this::returnResult);
    }

    @RequestMapping(path = "/versions", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> listVersionsBySearch(
            @Valid @RequestBody RepositoryDTO request) throws IOException {
        CompletableFuture<ResponseDTO<List<PackageVersionDTO>>> builds = searchService.searchVersions(request);
        return builds.thenApply(this::returnResult);
    }
}
