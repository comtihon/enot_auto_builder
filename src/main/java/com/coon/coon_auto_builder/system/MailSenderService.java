package com.coon.coon_auto_builder.system;

import com.coon.coon_auto_builder.domain.BuildResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailSenderService {

    @Autowired
    MailConfiguration configuration;

    @Autowired
    MessageChannel smtpChannel;

    public void sendReport(String to, String service, Status status, List<BuildResult> results) {
        String subject;
        if (status == Status.FINISHED) { // TODO build with all failed results should be considered failed.
            subject = "Coon build for " + service + " succeed";
        } else
            subject = "Coon build for " + service + " failed";

        //TODO form payload via Thymeleaf
        Message<String> message = MessageBuilder
                .withPayload("pam-pam-paaam")
                .setHeader("Subject", subject)
                .setHeader("To", to)
                .setHeader("FROM", configuration.getUser())
                .build();
        smtpChannel.send(message);
    }
}
