package com.coon.coon_auto_builder.data.model;

public interface Task {
    void process();

    MailReport generateEmail();

    String key();
}
