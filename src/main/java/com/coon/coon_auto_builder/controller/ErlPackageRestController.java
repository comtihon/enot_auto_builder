package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.service.BuildSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class ErlPackageRestController extends AbstractController {

    @Autowired
    private BuildSearchService buildSearchService;

    @RequestMapping(path = "/search", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> listBySearch(
            @Valid @RequestBody RepositoryDTO request) throws IOException {
        CompletableFuture<ResponseDTO<List<BuildDTO>>> builds = buildSearchService.fetchBuilds(request);
        return builds.thenApply(this::returnResult);
    }
}
