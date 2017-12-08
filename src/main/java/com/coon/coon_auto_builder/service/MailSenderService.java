package com.coon.coon_auto_builder.service;

import com.coon.coon_auto_builder.config.MailConfiguration;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.entity.PackageVersion;
import com.coon.coon_auto_builder.data.entity.Repository;
import com.coon.coon_auto_builder.service.dto.MailReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MailSenderService {
    @Autowired
    private MailConfiguration configuration;

    @Autowired
    private MessageChannel smtpChannel;

    @Autowired
    private TemplateEngine emailTemplateEngine;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Send report with all builds information.
     *
     * @param repository with versions and builds information
     */
    public void sendReport(Repository repository) {
        mails(repository).values().stream().filter(report -> report.getTo() != null).forEach(this::send);
    }

    private Map<String, MailReportDTO> mails(Repository repository) {
        Map<String, MailReportDTO> mails = new HashMap<>();
        Type listType = new TypeToken<List<BuildDTO>>() {
        }.getType();
        for (PackageVersion version : repository.getVersions()) {
            if (mails.containsKey(version.getEmail())) {
                List<BuildDTO> dtos = modelMapper.map(version.getBuildsRes(), listType);
                mails.get(version.getEmail()).addBuilds(version.getErlVersion(), dtos);
            } else {
                MailReportDTO report = new MailReportDTO(version.getEmail(), repository.getName(), version.getRef());
                report.addBuilds(version.getErlVersion(), modelMapper.map(version.getBuildsRes(), listType));
                mails.put(version.getEmail(), report);
            }
        }
        return mails;
    }

    private void send(MailReportDTO report) {
        report.calculateSuccess();
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
        log.debug("Send report to {}", report.getTo());
        smtpChannel.send(message);
    }
}
