package com.coon.coon_auto_builder.model;

import com.coon.coon_auto_builder.system.ProcessException;
import com.coon.coon_auto_builder.system.ServerConfiguration;
import com.coon.coon_auto_builder.system.Status;

public interface Task {
    void process() throws ProcessException;

    void sendEmail();

    String key();
}
