package com.coon.coon_auto_builder.model;

import com.coon.coon_auto_builder.system.ProcessException;
import com.coon.coon_auto_builder.system.ServerConfiguration;
import com.coon.coon_auto_builder.system.Status;

public interface Task {
    void setStatus(Status status);

    Status getStatus();

    void process(ServerConfiguration configuration) throws ProcessException;

    void email();
}
