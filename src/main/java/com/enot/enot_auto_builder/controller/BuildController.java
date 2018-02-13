package com.enot.enot_auto_builder.controller;

import com.enot.enot_auto_builder.controller.dto.ResponseDTO;
import com.enot.enot_auto_builder.data.dto.BuildDTO;
import com.enot.enot_auto_builder.data.dto.RepositoryDTO;
import com.enot.enot_auto_builder.data.dto.RepositoryGithubDTO;
import com.enot.enot_auto_builder.data.dto.Validatable;
import com.enot.enot_auto_builder.service.BuildRequestValidator;
import com.enot.enot_auto_builder.service.build.BuildService;
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
     * @throws Exception
     */
    @RequestMapping(path = "/buildAsync", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> build(@RequestBody RepositoryDTO body) throws Exception {
        return processBody(body);
    }

    /**
     * Manual build request. Sync
     *
     * @param body json with repository to build
     * @return build result
     */
    @RequestMapping(path = "/buildSync", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> buildSync(@RequestBody RepositoryDTO body) {
        return processSyncBody(body);
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

    private CompletableFuture<ResponseEntity<?>> processBody(Validatable body) throws Exception {
        CompletableFuture<ResponseDTO> validation = validator.validate(body);
        ResponseDTO validated = validation.get();
        if (validated.isResult())
            manager.buildAsync((RepositoryDTO) validated.getResponse());
        return validation.thenApply(this::returnResult);
    }

    private CompletableFuture<ResponseEntity<?>> processSyncBody(Validatable body) {
        CompletableFuture<ResponseDTO> request = validator.validate(body);
        return request.thenApply(validated -> {
            if (validated.isResult()) {
                return manager.buildSync((RepositoryDTO) validated.getResponse());
            }
            return validated;
        }).thenApply(this::returnResult);
    }
}
