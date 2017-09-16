package com.coon.coon_auto_builder.system;

import com.coon.coon_auto_builder.data.model.MailReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailSenderService {

    @Autowired
    MailConfiguration configuration;

    @Autowired
    MessageChannel smtpChannel;

    @Autowired
    TemplateEngine emailTemplateEngine;

    public void sendReport(MailReport report) {
        final String htmlContent = emailTemplateEngine.process("build_notification",
                report.getContext());

        Message<String> message = MessageBuilder
                .withPayload(htmlContent)
                .setHeader(MailHeaders.SUBJECT, report.getSubject())
                .setHeader(MailHeaders.CONTENT_TYPE, "text/html")
                .setHeader(MailHeaders.TO, report.getTo())
                .setHeader(MailHeaders.FROM, configuration.getUser())
                .build();
        smtpChannel.send(message);
    }
}
