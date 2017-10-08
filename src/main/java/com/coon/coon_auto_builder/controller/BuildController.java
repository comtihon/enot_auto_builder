package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryGithubDTO;
import com.coon.coon_auto_builder.data.dto.Validatable;
import com.coon.coon_auto_builder.service.build.BuildService;
import com.coon.coon_auto_builder.service.BuildRequestValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

@RestController
public class BuildController extends AbstractController {
    @Autowired
    private BuildService manager;

    @Autowired
    private BuildRequestValidator validator;

    /**
     * Manual build/rebuild request. Can be sent via gui.
     *
     * @param body json with repository to build
     */
    @RequestMapping(path = "/buildAsync", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> build(@RequestBody RepositoryDTO body) throws Exception {
        return processBody(body);
    }

    /**
     * Manual rebuild request. Can be sent via gui.
     */
    @RequestMapping(path = "/rebuild", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> rebuild(
            @Valid @RequestBody BuildDTO body) throws Exception {
        return processBody(body);
    }


    /**
     * Hook from github. Should be triggered on every tag push.
     *
     * @param signature x-hub-signature header to be checked
     * @param bodyStr   all the body payload
     */
    @RequestMapping(path = "/callback", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> buildFromGithub(
            @RequestHeader(name = "x-hub-signature") String signature,
            @RequestBody String bodyStr) throws Exception {
        return processBody(new RepositoryGithubDTO(signature, bodyStr));
    }

    @NotNull
    private CompletableFuture<ResponseEntity<?>> processBody(Validatable body) throws Exception {
        CompletableFuture<ResponseDTO> validation = validator.validate(body);
        ResponseDTO validated = validation.get();
        if (validated.isResult())
            manager.buildAsync((RepositoryDTO)validated.getResponse());
        return validation.thenApply(this::returnResult);
    }
}
