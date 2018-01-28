package com.coon.coon_auto_builder.data.dto;

import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.entity.Repository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import static com.coon.coon_auto_builder.tool.UrlHelper.removeGitEnding;

/**
 * Request from github
 */
@Slf4j
public class RepositoryGithubDTO extends RepositoryDTO {
    private String signature;
    private String body;

    public RepositoryGithubDTO(String signature, String body) throws IOException {
        this.signature = signature;
        this.body = body;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(body);
        JsonNode repoNode = rootNode.path("repository");
        PackageVersionDTO pv = new PackageVersionDTO(rootNode.path("ref").asText());
        this.refType = rootNode.path("ref_type").asText();
        this.fullName = repoNode.path("full_name").asText();
        this.cloneUrl = removeGitEnding(repoNode.path("clone_url").asText());
        this.versions = Collections.singletonList(pv);
    }

    @Override
    public void basicValidation(String secret) throws Exception {
        if (!isGithub(this.cloneUrl))
            throw new Exception("Url " + this.cloneUrl + " doesn't point to github!");
        if (!isGithubMatch(this.cloneUrl, this.fullName))
            throw new Exception("Malformed github url: " + this.cloneUrl);
        checkSignature(secret);
    }

    @Override
    public void onConflict(Repository found, RepositoryDAOService service) throws Exception {
        if (found != null) { // if there is a repo with same namespace but different url
            log.warn("Found collision " + this + " with previously saved " + found);
            service.delete(found.getUrl());
            //TODO should we notify malformed repo owner?
        }
    }

    private boolean isGithub(String url) {
        return url.startsWith("https://github.com/");
    }

    private boolean isGithubMatch(String url, String fullName) {
        return ("https://github.com/" + fullName).equals(url);
    }

    private void checkSignature(String secret) throws Exception {
        if (signature == null
                || body == null
                || this.cloneUrl == null
                || secret == null
                || secret.isEmpty())
            throw new Exception("Can't verify github signature");
        Mac mac = initMac(secret);
        final char[] hash = Hex.encodeHex(mac.doFinal(body.getBytes()));
        final String expected = "sha1=" + String.valueOf(hash);
        log.debug("Comparing {} and {}", expected, signature);
        if (!expected.equals(signature))
            throw new Exception("Wrong signature for " + this.fullName);
    }

    private Mac initMac(String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        final Mac mac = Mac.getInstance("HmacSHA1");
        final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
        mac.init(signingKey);
        return mac;
    }
}
