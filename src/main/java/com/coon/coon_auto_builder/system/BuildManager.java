package com.coon.coon_auto_builder.system;

import com.coon.coon_auto_builder.model.Task;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BuildManager {
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    @Async
    public void process(final Task request) {
        tasks.put(request.key(), request);
        try {
            request.process();
        } catch (ProcessException e) {
            request.sendEmail();
        } finally {
            tasks.remove(request.key());
        }
    }
}
