package com.coon.coon_auto_builder.system;

import com.coon.coon_auto_builder.data.model.MailReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

@Service
public class MailSenderService {

    @Autowired
    MailConfiguration configuration;

    @Autowired
    MessageChannel smtpChannel;

    public void sendReport(MailReport report) {
        String subject;
        if (report.isSuccesss()) {
            subject = "Coon build for " + report.getPackageName() + " succeed";
        } else
            subject = "Coon build for " + report.getPackageName() + " failed";

        //TODO form payload via Thymeleaf
        Message<String> message = MessageBuilder
                .withPayload(report.getBody())
                .setHeader(MailHeaders.SUBJECT, subject)
                .setHeader(MailHeaders.TO, report.getTo())
                .setHeader(MailHeaders.FROM, configuration.getUser())
                .build();
        smtpChannel.send(message);
    }
}
