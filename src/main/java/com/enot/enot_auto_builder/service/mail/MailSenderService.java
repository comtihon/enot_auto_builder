package com.enot.enot_auto_builder.service.mail;

import com.enot.enot_auto_builder.config.MailConfiguration;
import com.enot.enot_auto_builder.data.dto.BuildDTO;
import com.enot.enot_auto_builder.data.entity.Build;
import com.enot.enot_auto_builder.data.entity.PackageVersion;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;

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
    @Setter //for test
    private ModelMapper modelMapper;

    /**
     * Send report with all builds information.
     *
     * @param builds builds information
     */
    public void sendReport(List<Build> builds) {
        builds.stream()
                .filter(build -> build.getPackageVersion().getEmail() != null)
                .collect(HashMap::new, this::addBuild, this::mergeBuilds)
                .values()
                .forEach(this::send);
    }

    void addBuild(HashMap<String, MailReportDTO> acc, Build build) {
        BuildDTO dto = modelMapper.map(build, BuildDTO.class);
        PackageVersion pv = build.getPackageVersion();
        String email = pv.getEmail();
        if (acc.containsKey(email))
            acc.get(email).addBuild(pv.getErlVersion(), dto);
        else {
            MailReportDTO report = new MailReportDTO(email, pv.getRepository().getName(), pv.getRef());
            report.addBuild(pv.getErlVersion(), dto);
            acc.put(email, report);
        }
    }

    void mergeBuilds(HashMap<String, MailReportDTO> acc1, HashMap<String, MailReportDTO> acc2) {
        acc2.forEach(
                (e, m) ->
                        acc1.merge(e, m,
                                (v1, v2) ->
                                {
                                    v1.getResults().putAll(v2.getResults());
                                    return v1;
                                }));
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
