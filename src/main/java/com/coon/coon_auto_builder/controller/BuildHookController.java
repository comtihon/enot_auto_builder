package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.data.dto.BuildRequestDTO;
import com.coon.coon_auto_builder.data.model.BuildRequest;
import com.coon.coon_auto_builder.system.BuildManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BuildHookController {

    @Autowired
    private BuildManager manager;

    @Autowired
    private ApplicationContext appContext;

    @RequestMapping(path = "/process", method = RequestMethod.POST)
    public String build(@RequestBody BuildRequestDTO body) {
        System.out.println("body = " + body);
        BuildRequest request = getBuildRequst();
        request.init(body); //TODO refactor
        manager.process(request);
        return "OK";
    }

    private BuildRequest getBuildRequst() {
        return appContext.getBean(BuildRequest.class);
    }

}
