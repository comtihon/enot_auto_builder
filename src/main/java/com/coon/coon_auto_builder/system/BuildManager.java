package com.coon.coon_auto_builder.system;

import com.coon.coon_auto_builder.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class BuildManager {
    @Autowired
    private ServerConfiguration configuration;

    @Async
    public void process(final Task request) {
        request.setStatus(Status.WAIT);
        //TODO save request somewhere before building
        try {
            request.process(configuration);
        } catch (ProcessException e) {
            e.printStackTrace();  //TODO collect error stack trace
            request.setStatus(Status.ERROR);
            request.email(); //TODO String message?
        } // TODO finally remove from executing?
    }
}
