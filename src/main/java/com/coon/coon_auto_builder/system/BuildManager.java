package com.coon.coon_auto_builder.system;

import com.coon.coon_auto_builder.data.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BuildManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    @Autowired
    private MailSenderService mailSender;

    @Async
    public void process(final Task request) {
        tasks.put(request.key(), request);
        try {
            request.process();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Task processing error " + e.getMessage());
        } finally {
            tasks.remove(request.key());
            mailSender.sendReport(request.generateEmail());
        }
    }
}
