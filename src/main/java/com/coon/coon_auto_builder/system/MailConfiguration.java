package com.coon.coon_auto_builder.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mail.MailSendingMessageHandler;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.MessageChannel;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import java.util.Properties;

@Configuration
@EnableIntegration
public class MailConfiguration {
    @Value("${email.host}")
    private String host;
    @Value("${email.port}")
    private int port;
    @Value("${email.username}")
    private String user;
    @Value("${email.password}")
    private String pass;

    public String getUser() {
        return user;
    }

    @Bean
    public MessageChannel smtpChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "smtpChannel")
    public MailSendingMessageHandler mailSendingMessageHandler() throws Exception {
        return new MailSendingMessageHandler(mailSender());
    }

    @Bean
    public MailSender mailSender() throws Exception {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(user);
        mailSender.setPassword(pass);
        mailSender.setJavaMailProperties(additionalMailProperties());
        return mailSender;
    }

    private Properties additionalMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        return properties;
    }
}
