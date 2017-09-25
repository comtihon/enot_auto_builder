package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.dto.BuildRequestDTO;
import com.coon.coon_auto_builder.data.dto.GithubRequestDTO;
import com.coon.coon_auto_builder.data.dto.RebuildRequestDTO;
import com.coon.coon_auto_builder.data.model.BuildRequest;
import com.coon.coon_auto_builder.system.BuildManager;
import com.coon.coon_auto_builder.system.MailSenderService;
import com.coon.coon_auto_builder.system.ServerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class BuildController {
    @Autowired
    private ServerConfiguration configuration;

    @Autowired
    private BuildManager manager;

    @Autowired
    private BuildDAOService buildDAOService;

    @Autowired
    private RepositoryDAOService repositoryDAOService;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private MailSenderService mailSenderService;

    /**
     * Manual build/rebuild request. Can be sent via gui.
     *
     * @param body json with repository to build
     * @return OK
     */
    @RequestMapping(path = {"/process", "/rebuild"}, method = RequestMethod.POST)
    public ResponseEntity<?> build(@RequestBody BuildRequestDTO body) {
        return processBody(body);
    }

    /**
     * Manual rebuild request. Can be sent via gui.
     *
     * @param buildId id of a build being rebuilt
     * @return OK
     */
    @RequestMapping(path = "/rebuild", method = RequestMethod.GET)
    public ResponseEntity<?> rebuild(@RequestParam(value = "build_id") String buildId) {
        return processBody(new RebuildRequestDTO(buildId, buildDAOService));
    }


    /**
     * Hook from github. Should be triggered on every tag push.
     *
     * @param signature x-hub-signature header to be checked
     * @param bodyStr   all the body payload
     * @return OK or bad request in case of signature check fails
     */
    @RequestMapping(path = "/callback", method = RequestMethod.POST)
    public ResponseEntity<?> buildFromGithub(
            @RequestHeader(name = "x-hub-signature") String signature,
            @RequestBody String bodyStr) {
        try {
            return processBody(
                    new GithubRequestDTO(signature, configuration.getGitubSecret(), bodyStr, mailSenderService));
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Allow:
     * 1. Request from github if signature presents and correct
     * 2. Rebuild existing build request
     * 3. Build existing repository
     * 4. New build (not from github) if doesn't have namespace/name clash with
     * existing repository.
     */
    @NotNull
    private ResponseEntity<?> processBody(BuildRequestDTO body) {
        body.setServiceOnce(repositoryDAOService);
        try {
            body.validate();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        BuildRequest request = body.toBuildRequest(appContext, configuration);
        manager.process(request);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
