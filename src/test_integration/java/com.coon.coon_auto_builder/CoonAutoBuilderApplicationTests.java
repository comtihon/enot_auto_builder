package com.coon.coon_auto_builder;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.service.git.GitService;
import com.coon.coon_auto_builder.service.mail.MailSenderService;
import com.coon.coon_auto_builder.service.build.Builder;
import com.coon.coon_auto_builder.service.git.ClonedRepo;
import com.coon.coon_auto_builder.service.loader.Loader;
import com.coon.coon_auto_builder.service.loader.LoaderFactory;
import com.coon.coon_auto_builder.service.tool.Coon;
import com.coon.coon_auto_builder.service.tool.Erlang;
import com.coon.coon_auto_builder.service.tool.Kerl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@Slf4j
public class CoonAutoBuilderApplicationTests extends IntegrationTest {

    private static final String NORMAL_CONF = "{\"name\":\"test\",\"fullname\":\"comtihon/test\",\"app_vsn\":\"1.0.0\"}";
    private static final String MULTIPLE_ERL_CONF =
            "{\"name\":\"test\",\"fullname\":\"comtihon/test\",\"app_vsn\":\"1.0.0\",\"erlang\":[\"18\",\"19\",\"20\"]}";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RepositoryDAOService repositoryDAOService;

    @Value("${default_erlang}")
    private String erlangVersion;

    @MockBean
    private GitService gitService;

    @MockBean
    private Coon coon;

    @MockBean
    private Kerl kerl;

    @MockBean
    private LoaderFactory loaderFactory;

    @MockBean
    private Loader loader;

    @MockBean
    private MailSenderService mailSender;

    private CountDownLatch startSearch;

    private List notified;

    @Before
    public void setUp() throws Exception {
        super.setUp(gitService, coon, kerl, loader, erlangVersion, startSearch, mailSender);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            notified = (List) args[0];
            startSearch.countDown();
            return null;
        }).when(mailSender).sendReport(any());
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File("test/tmp"));
        repositoryDAOService.getAll().forEach(repo -> repositoryDAOService.delete(repo.getUrl()));
    }

    /**
     * Add order to build test repository with default Erlang.
     * Search built package after build completed.
     *
     * @throws InterruptedException in latch await
     */
    @Test
    public void testNormalBuild() throws InterruptedException, IOException {
        writeApp("test/tmp", "test", "1.0.0", NORMAL_CONF);
        startSearch = new CountDownLatch(1);
        RepositoryDTO repo = new RepositoryDTO("comtihon/test",
                "https://github.com/comtihon/test.git",
                new PackageVersionDTO("1.0.0"));
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildAsync", repo, ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        startSearch.await(30, TimeUnit.SECONDS);
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=test", ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        List<LinkedHashMap> packages = (List<LinkedHashMap>) searchResponse.getResponse();
        Assert.assertEquals(1, packages.size());
        LinkedHashMap packageDTO = packages.get(0);
        Assert.assertEquals("test", packageDTO.get("name"));
        Assert.assertEquals("comtihon", packageDTO.get("namespace"));
        Assert.assertTrue((Boolean) packageDTO.get("success"));
        Assert.assertEquals("/download/" + packageDTO.get("build_id"), packageDTO.get("path"));
        Assert.assertNotNull(notified); //notification was sent with proper version
        Assert.assertEquals(1, notified.size());
        Assert.assertEquals(((Build)notified.get(0)).getBuildId(), packageDTO.get("build_id"));
    }

    @Test
    public void testMultipleErlangBuild() throws IOException, InterruptedException {
        writeApp("test/tmp", "test", "1.0.0", MULTIPLE_ERL_CONF);
        startSearch = new CountDownLatch(1);
        RepositoryDTO repo = new RepositoryDTO("comtihon/test",
                "https://github.com/comtihon/test.git",
                new PackageVersionDTO("1.0.0"));
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildAsync", repo, ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        startSearch.await(30, TimeUnit.SECONDS);
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=test", ResponseDTO.class);
        Assert.assertTrue(searchResponse.isResult());
        List<LinkedHashMap> packages = (List<LinkedHashMap>) searchResponse.getResponse();
        Assert.assertEquals(3, packages.size());
    }

    @Test
    public void testBuildNoEmailNotification() throws IOException, InterruptedException {
        writeApp("test/tmp", "test", "1.0.0", MULTIPLE_ERL_CONF);
        RepositoryDTO repo = new RepositoryDTO("comtihon/test",
                "https://github.com/comtihon/test.git",
                new PackageVersionDTO("1.0.0"));
        repo.setNotifyEmail(false);
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildAsync", repo, ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        Thread.sleep(100);
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=test", ResponseDTO.class);
        Assert.assertTrue(searchResponse.isResult());
        verify(mailSender, never()).sendReport(any());
    }

    @Test
    public void testBuildSeveralFailOne() throws Exception {
        writeApp("test/tmp", "test", "1.0.0", MULTIPLE_ERL_CONF);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            String erlangExecutable = (String)args[1];
            if (erlangExecutable.equals("path/to/19"))
                throw new RuntimeException("build failed");
            return null;
        }).when(coon).build(any(), any());

        startSearch = new CountDownLatch(1);
        RepositoryDTO repo = new RepositoryDTO("comtihon/test",
                "https://github.com/comtihon/test.git",
                new PackageVersionDTO("1.0.0"));
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildAsync", repo, ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        startSearch.await(30, TimeUnit.SECONDS);
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=test", ResponseDTO.class);
        Assert.assertTrue(searchResponse.isResult());
        List<LinkedHashMap> packages = (List<LinkedHashMap>) searchResponse.getResponse();
        Assert.assertEquals(3, packages.size());
    }

    @Test
    public void testSyncBuildReturnError() throws Exception {
        writeApp("test/tmp", "test", "1.0.0", MULTIPLE_ERL_CONF);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            String erlangExecutable = (String)args[1];
            if (erlangExecutable.equals("path/to/19"))
                throw new RuntimeException("build failed");
            return null;
        }).when(coon).build(any(), any());

        startSearch = new CountDownLatch(1);
        RepositoryDTO repo = new RepositoryDTO("comtihon/test",
                "https://github.com/comtihon/test.git",
                new PackageVersionDTO("1.0.0"));
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildSync", repo, ResponseDTO.class);
        Assert.assertTrue(!responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        Assert.assertEquals("build failed\n", responseDTO.getResponse());
    }
}
