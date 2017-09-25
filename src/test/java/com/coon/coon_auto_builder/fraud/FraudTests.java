package com.coon.coon_auto_builder.fraud;

import com.coon.coon_auto_builder.HibernateTestConfig;
import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.dto.BuildRequestDTO;
import com.coon.coon_auto_builder.data.dto.GithubRequestDTO;
import com.coon.coon_auto_builder.data.dto.RebuildRequestDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import com.coon.coon_auto_builder.system.MailSenderService;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.crypto.Mac;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {HibernateTestConfig.class})
@Transactional
public class FraudTests {

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Mock
    MailSenderService service;

    @Autowired
    RepositoryDAOService repositoryDAO;

    @Autowired
    BuildDAOService buildDAOService;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void validateRebuild() throws Exception {
        RebuildRequestDTO request = new RebuildRequestDTO("unknown", buildDAOService);
        Assert.isTrue(assertThrown(request, "No such build!"),
                "Rebuild request with unknown build id should not pass validation");
        request = new RebuildRequestDTO("build_id1", buildDAOService);
        Assert.isTrue(!assertThrown(request, ""),
                "Rebuild request with existing build id should pass");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void validateManualBuild() {
        RepositoryDTO repo = new RepositoryDTO("namespace/name", "url");
        BuildRequestDTO request = new BuildRequestDTO("1.0.0", "tag", repo, null, repositoryDAO);
        Assert.isTrue(!assertThrown(request, ""), "Empty repo should be saved");
        repo = new RepositoryDTO("namespace1/name1", "url1");
        request = new BuildRequestDTO("1.0.0", "tag", repo, null, repositoryDAO);
        Assert.isTrue(!assertThrown(request, ""), "No collision repo should be saved");
        repo = new RepositoryDTO("namespace1/name1", "malformed_url");
        request = new BuildRequestDTO("1.0.0", "tag", repo, null, repositoryDAO);
        Assert.isTrue(assertThrown(request,
                "Request BuildRequestDTO{ref='1.0.0', repository=RepositoryDTO{fullName='namespace1/name1', " +
                        "cloneUrl='malformed_url'}} tries to overwrite RepositoryBO{url='url1', name='name1', " +
                        "namespace='namespace1'}"), "Repo's url is malformed. Should not be saved");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void validateGithubBuild() throws Exception {
        String body = "{\"repository\":{\"clone_url\":\"url\",\"full_name\":\"namespace/name\"}," +
                "\"ref\":\"1.0.0\", \"ref_type\":\"tag\"}";
        GithubRequestDTO request = new GithubRequestDTO("signature", "secret", body, service);
        Assert.isTrue(assertThrown(request,
                "Url url doesn't point to github!"), "Repo doesn't belong to github");
        body = "{\"repository\":{\"clone_url\":\"https://github.com/a/b\",\"full_name\":\"namespace/name\"}," +
                "\"ref\":\"1.0.0\", \"ref_type\":\"tag\"}";
        request = new GithubRequestDTO("signature", "secret", body, service);
        Assert.isTrue(assertThrown(request,
                "Malformed github url: https://github.com/a/b"),
                "Should not allow malformed github urls");
        body = "{\"repository\":{\"clone_url\":\"https://github.com/namespace/name\",\"full_name\":\"namespace/name\"}," +
                "\"ref\":\"1.0.0\", \"ref_type\":\"tag\"}";
        request = new GithubRequestDTO("signature", "secret", body, service);
        Assert.isTrue(assertThrown(request,
                "Wrong signature for namespace/name"),
                "Should not allow requests with wrong signature");
        String expected = secretExpected(body, request);
        request = new GithubRequestDTO(expected, "secret", body, service, repositoryDAO);
        Assert.isTrue(!assertThrown(request, ""), "No conflicts - should pass");
        body = "{\"repository\":{\"clone_url\":\"https://github.com/namespace1/name1\",\"full_name\":\"namespace1/name1\"}," +
                "\"ref\":\"1.0.0\", \"ref_type\":\"tag\"}";
        expected = secretExpected(body, request);
        request = new GithubRequestDTO(expected, "secret", body, service, repositoryDAO);
        Optional<RepositoryBO> find = repositoryDAO.findByNameAndNamespace("name1", "namespace1");
        Assert.isTrue(find.isPresent(), "conflicted repo should be found");
        Assert.isTrue(!assertThrown(request, ""), "No throw conflict for github. Just overwrite");
        Mockito.verify(service).sendOnConflict(request, find.get(), true);
    }

    private boolean assertThrown(BuildRequestDTO request, String message) {
        try {
            request.validate();
            return false;
        } catch (Exception e) {
            Assert.isTrue(e.getMessage().equals(message),
                    "expected: " + e.getMessage() + "\ngot: " + message);
            return true;
        }
    }

    private String secretExpected(String body, GithubRequestDTO request)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = request.getClass().getDeclaredMethod("initMac", String.class);
        method.setAccessible(true);
        Mac mac = (Mac) method.invoke(request, "secret");
        final char[] hash = Hex.encodeHex(mac.doFinal(body.getBytes()));
        return "sha1=" + String.valueOf(hash);
    }
}
