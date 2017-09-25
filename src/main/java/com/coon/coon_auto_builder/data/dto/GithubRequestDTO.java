package com.coon.coon_auto_builder.data.dto;

import com.coon.coon_auto_builder.data.dao.DaoService;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import com.coon.coon_auto_builder.system.MailSenderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Request from github
 */
public class GithubRequestDTO extends BuildRequestDTO {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String signature;
    private String body;
    private String secret;
    private MailSenderService mailSenderService;

    public GithubRequestDTO(String signature, String secret,
                            String body, MailSenderService mailSender) throws IOException {
        this.signature = signature;
        this.body = body;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(body);
        this.repository = objectMapper.readValue(rootNode.path("repository").toString(), RepositoryDTO.class);
        this.ref = rootNode.path("ref").asText();
        this.refType = rootNode.path("ref_type").asText();
        this.secret = secret;
        this.mailSenderService = mailSender;
    }

    public GithubRequestDTO(String signature, String secret,
                            String body, MailSenderService mailSender, DaoService dao) throws IOException {
        this(signature, secret, body, mailSender);
        this.service = dao;
    }

    @Override
    public void validate() throws Exception {
        if (!isGithub(getUrl()))
            throw new Exception("Url " + getUrl() + " doesn't point to github!");
        if (!isGithubMatch(getUrl(), getName()))
            throw new Exception("Malformed github url: " + getUrl());
        checkSignature();
        RepositoryBO repo = findCollision(service);
        if (repo != null) { // if there is a repo with same namespace but different url
            logger.warn("Found collision " + this + " with previously saved " + repo);
            service.delete(repo.getUrl());
            mailSenderService.sendOnConflict(this, repo, true);
        }
    }

    private boolean isGithub(String url) {
        return url.startsWith("https://github.com/");
    }

    private boolean isGithubMatch(String url, String name, String namespace) {
        return isGithubMatch(url, namespace + "/" + name);
    }

    private boolean isGithubMatch(String url, String fullName) {
        return ("https://github.com/" + fullName).equals(url);
    }

    private void checkSignature() throws Exception {
        if (signature == null
                || body == null
                || getUrl() == null
                || secret == null
                || secret.isEmpty())
            throw new Exception("Can't verify github signature");
        Mac mac = initMac(secret);
        final char[] hash = Hex.encodeHex(mac.doFinal(body.getBytes()));
        final String expected = "sha1=" + String.valueOf(hash);
        logger.debug("Comparing {} and {}", expected, signature);
        if (!expected.equals(signature))
            throw new Exception("Wrong signature for " + getName());
    }

    private Mac initMac(String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac mac = Mac.getInstance("HmacSHA1");
        final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
        mac.init(signingKey);
        return mac;
    }
}
