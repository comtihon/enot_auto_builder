package com.coon.coon_auto_builder.service;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryGithubDTO;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class BuildRequestValidatorTest {
    @Autowired
    private BuildRequestValidator validator;

    @Value("${github_secret:null}")
    private String secret;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void validateRebuild() throws Exception {
        //Unknown build id - Fail
        BuildDTO request = new BuildDTO();
        request.setBuildId("unknown");
        CompletableFuture<ResponseDTO> result = validator.validate(request);
        ResponseDTO responseDTO = result.get();
        Assert.assertFalse(responseDTO.isResult());
        Assert.assertEquals("No such build!", responseDTO.getResponse());

        //Existent build rebuild - OK
        request = new BuildDTO();
        request.setBuildId("build_id1");
        result = validator.validate(request);
        responseDTO = result.get();
        Assert.assertTrue(responseDTO.isResult());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void validateManualBuild() throws Exception {
        //save new - OK
        PackageVersionDTO pv = new PackageVersionDTO("1.0.0", "18");
        RepositoryDTO request = new RepositoryDTO("namespace/name", "url", pv);
        CompletableFuture<ResponseDTO> result = validator.validate(request);
        Assert.assertTrue(result.get().isResult());

        //save save - OK
        request = new RepositoryDTO("namespace1/name1", "url1", pv);
        result = validator.validate(request);
        Assert.assertTrue(result.get().isResult());

        //save collision - Fail
        request = new RepositoryDTO("namespace1/name1", "malformed", pv);
        result = validator.validate(request);
        Assert.assertFalse(result.get().isResult());
        Assert.assertEquals(
                "Request RepositoryDTO{ref='1.0.0', repository=RepositoryDTO{fullName='namespace1/name1', " +
                        "cloneUrl='malformed_url'}} tries to overwrite RepositoryBO{url='url1', name='name1', " +
                        "namespace='namespace1'}",
                result.get().getResponse());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void validateGithubBuild() throws Exception {
        //no github url - Fail
        String body = "{\"repository\":{\"clone_url\":\"url\",\"full_name\":\"namespace/name\"}," +
                "\"ref\":\"1.0.0\", \"ref_type\":\"tag\"}";
        RepositoryGithubDTO request = new RepositoryGithubDTO("signature", body);
        CompletableFuture<ResponseDTO> result = validator.validate(request);
        Assert.assertFalse(result.get().isResult());
        Assert.assertEquals("Url url doesn't point to github!", result.get().getResponse());

        //malformed github url - Fail
        body = "{\"repository\":{\"clone_url\":\"https://github.com/a/b\",\"full_name\":\"namespace/name\"}," +
                "\"ref\":\"1.0.0\", \"ref_type\":\"tag\"}";
        request = new RepositoryGithubDTO("signature", body);
        result = validator.validate(request);
        Assert.assertFalse(result.get().isResult());
        Assert.assertEquals("Malformed github url: https://github.com/a/b", result.get().getResponse());

        //wrong signature - Fail
        body = "{\"repository\":{\"clone_url\":\"https://github.com/namespace/name\",\"full_name\":\"namespace/name\"}," +
                "\"ref\":\"1.0.0\", \"ref_type\":\"tag\"}";
        request = new RepositoryGithubDTO("signature", body);
        result = validator.validate(request);
        Assert.assertFalse(result.get().isResult());
        Assert.assertEquals("Wrong signature for namespace/name", result.get().getResponse());

        //right signature - OK
        String expected = secretExpected(body, request);
        request = new RepositoryGithubDTO(expected, body);
        result = validator.validate(request);
        Assert.assertTrue(result.get().isResult());
    }

    private String secretExpected(String body, RepositoryGithubDTO request)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = request.getClass().getDeclaredMethod("initMac", String.class);
        method.setAccessible(true);
        Mac mac = (Mac) method.invoke(request, secret);
        final char[] hash = Hex.encodeHex(mac.doFinal(body.getBytes()));
        return "sha1=" + String.valueOf(hash);
    }

}