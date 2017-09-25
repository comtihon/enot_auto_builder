package com.coon.coon_auto_builder.system;

import com.coon.coon_auto_builder.data.dto.BuildRequestDTO;
import com.coon.coon_auto_builder.data.model.MailReport;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class MailSenderService {

    @Autowired
    MailConfiguration configuration;

    @Autowired
    MessageChannel smtpChannel;

    @Autowired
    TemplateEngine emailTemplateEngine;

    /**
     * Send report with all builds information.
     * @param report all builds, logs and artifacts
     */
    void sendReport(MailReport report) {
        final Context ctx = new Context();
        ctx.setVariable("report", report);
        ctx.setVariable("host", configuration.getServiceHost());
        final String htmlContent = emailTemplateEngine.process("build_notification", ctx);
        Message<String> message = MessageBuilder
                .withPayload(htmlContent)
                .setHeader(MailHeaders.SUBJECT, report.getSubject())
                .setHeader(MailHeaders.CONTENT_TYPE, "text/html")
                .setHeader(MailHeaders.TO, report.getTo())
                .setHeader(MailHeaders.FROM, configuration.getUser())
                .build();
        smtpChannel.send(message);
    }

    /**
     * Send conflict notification in case of fraud detected
     * @param first user's build request
     * @param second repository in the system, which causes a conflict
     * @param delete if repository second was deleted with all versions and builds
     */
    public void sendOnConflict(BuildRequestDTO first, RepositoryBO second, boolean delete) {
        final Context ctx = new Context();
        ctx.setVariable("first", first);
        ctx.setVariable("second", second);
        ctx.setVariable("delete", delete);
        final String htmlContent = emailTemplateEngine.process("fraud_notification", ctx);
        Message<String> message = MessageBuilder
                .withPayload(htmlContent)
                .setHeader(MailHeaders.SUBJECT, "Your repository cause a build conflict")
                .setHeader(MailHeaders.CONTENT_TYPE, "text/html")
                .setHeader(MailHeaders.TO, second.getEmail())
                .setHeader(MailHeaders.FROM, configuration.getUser())
                .build();
        smtpChannel.send(message);
    }
}
