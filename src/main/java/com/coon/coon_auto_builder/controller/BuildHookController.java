package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.data.dto.BuildRequestDTO;
import com.coon.coon_auto_builder.data.model.BuildRequest;
import com.coon.coon_auto_builder.system.BuildManager;
import com.coon.coon_auto_builder.system.FraudDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class BuildHookController {
    @Autowired
    private FraudDetector fraudDetector;

    @Autowired
    private BuildManager manager;

    @Autowired
    private ApplicationContext appContext;

    //TODO need to protect from malformed build requests.
    @RequestMapping(path = "/process", method = RequestMethod.POST)
    public ResponseEntity<?> build(
            @RequestHeader(name = "x-hub-signature", required = false) String signature,
            @RequestBody BuildRequestDTO body) {
        try {
            fraudDetector.tryDetect(signature, body.getRepository());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        BuildRequest request = getBuildRequst(body);
        manager.process(request);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    private BuildRequest getBuildRequst(BuildRequestDTO body) {
        return appContext.getBean(BuildRequest.class, body);
    }

}
