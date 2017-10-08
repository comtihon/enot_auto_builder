package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.service.BuildSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class ErlPackageDownloadController extends AbstractController {

    @Autowired
    private BuildSearchService buildSearchService;

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public void downloadBySearch(HttpServletResponse response,
                                 @Valid @RequestBody RepositoryDTO request) throws Exception {
        CompletableFuture<ResponseDTO<List<BuildDTO>>> build = buildSearchService.fetchBuilds(request);
        ResponseDTO<List<BuildDTO>> responseDTO = build.get();
        if (responseDTO.isResult()) {
            List<BuildDTO> found = responseDTO.getResponse();
            found.sort(Comparator.comparing(BuildDTO::getCreatedDate));
            renderPackage(new ResponseDTO<>(found.get(0)), response);
        } else {
            renderPackage(build.get(), response);
        }
    }

    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    public void downloadById(HttpServletResponse response, @PathVariable String id) throws Exception {
        CompletableFuture<ResponseDTO> builds = buildSearchService.findBuild(id);
        renderPackage(builds.get(), response);
    }
}
