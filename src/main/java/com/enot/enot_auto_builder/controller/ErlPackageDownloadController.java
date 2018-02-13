package com.enot.enot_auto_builder.controller;

import com.enot.enot_auto_builder.controller.dto.ResponseDTO;
import com.enot.enot_auto_builder.data.dto.BuildDTO;
import com.enot.enot_auto_builder.data.dto.RepositoryDTO;
import com.enot.enot_auto_builder.service.SearchService;
import com.enot.enot_auto_builder.service.tool.Erlang;
import com.enot.enot_auto_builder.service.tool.Kerl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Controller
public class ErlPackageDownloadController extends AbstractController {

    private static final MediaType LOG_CONTENT_TYPE = new MediaType(MediaType.TEXT_PLAIN.getType(),
            MediaType.TEXT_PLAIN.getSubtype(),
            StandardCharsets.UTF_8);

    @Autowired
    private SearchService searchService;

    @Autowired
    private Kerl kerl;

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public void downloadBySearch(HttpServletResponse response,
                                 @Valid @RequestBody RepositoryDTO request) throws Exception {
        CompletableFuture<ResponseDTO> build = searchService.fetchBuild(request);
        renderPackage(build.get(), response);
    }

    @RequestMapping(value = DOWNLOAD_ID, method = RequestMethod.GET)
    public void downloadById(HttpServletResponse response, @PathVariable String id) throws Exception {
        CompletableFuture<ResponseDTO> builds = searchService.findBuild(id);
        renderPackage(builds.get(), response);
    }

    @RequestMapping(path = BUILD_LOG, method = RequestMethod.GET)
    public CompletableFuture<ResponseEntity<?>> getLogs(@RequestParam("build_id") String buildId) {
        CompletableFuture<ResponseDTO> pack = searchService.findBuild(buildId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(LOG_CONTENT_TYPE);
        return pack.thenApply(responseDTO -> {
            if (responseDTO.isResult()) {
                BuildDTO data = (BuildDTO) responseDTO.getResponse();
                return new ResponseEntity<>(data.getMessage(), headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(responseDTO.getResponse(), headers, HttpStatus.OK);
            }
        });
    }

    @RequestMapping(path = DOWNLOAD_ERTS, method = RequestMethod.GET)
    public void downloadErts(HttpServletResponse response, @PathVariable("version") String erlang) throws IOException {
        Erlang erl = kerl.getErlInstallations().get(erlang);
        if (erl == null) {
            renderPackage(new ResponseDTO<>(false, "No such erlang"), response);
        } else {
            renderPackage(new ResponseDTO<>(erl), response);
        }
    }
}
