package com.coon.coon_auto_builder;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dao.BuildDAOService;
import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.service.git.GitService;
import com.coon.coon_auto_builder.service.loader.Loader;
import com.coon.coon_auto_builder.service.loader.LoaderFactory;
import com.coon.coon_auto_builder.service.mail.MailSenderService;
import com.coon.coon_auto_builder.service.tool.Coon;
import com.coon.coon_auto_builder.service.tool.Kerl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@Slf4j
public class RecursiveBuildDepsTests extends IntegrationTest {
    private static final String DEP_LEAF_CONF = "{\"name\":\"dep\",\"fullname\":\"comtihon/dep\",\"app_vsn\":\"1.0.0\"}";
    private static final String DEP_TREE_CONF =
            "{\"name\":\"dep\",\"fullname\":\"comtihon/dep\",\"app_vsn\":\"1.0.0\",\"deps\":[" +
                    "{\"name\":\"dep2\",\"url\":\"https://github.com/comtihon/dep2\",\"tag\":\"1.0.0\"}]}";

    private static final String CONF_WITH_DEPS =
            "{\"name\":\"test\",\"fullname\":\"comtihon/test\",\"app_vsn\":\"1.0.0\",\"deps\":[" +
                    "{\"name\":\"dep1\",\"url\":\"https://github.com/comtihon/dep1\",\"tag\":\"1.0.0\"}," +
                    "{\"name\":\"dep2\",\"url\":\"https://github.com/comtihon/dep2\",\"tag\":\"master\"}," +
                    "{\"name\":\"dep3\",\"url\":\"https://github.com/comtihon/dep3\",\"tag\":\"1.0.0\"}" +
                    "]}";
    private static final String CONF_WITH_DEP =
            "{\"name\":\"test\",\"fullname\":\"comtihon/test\",\"app_vsn\":\"1.0.0\",\"deps\":[" +
                    "{\"name\":\"dep1\",\"url\":\"https://github.com/comtihon/dep1\",\"tag\":\"1.0.0\"}" +
                    "]}";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RepositoryDAOService repositoryDAOService;

    @Autowired
    private BuildDAOService daoService;

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

    @Before
    public void setUp() throws Exception {
        super.setUp(gitService, coon, kerl, loader, erlangVersion, mailSender);
    }

    @Test
    public void testBuildWithDeps() throws IOException, InterruptedException {
        CountDownLatch waitLatch = waitForBuild(2);
        writeApp("test/tmp", "test", "1.0.0", CONF_WITH_DEP);
        writeApp("test/tmp", "dep1", "1.0.0", DEP_LEAF_CONF);
        RepositoryDTO repo = RepositoryDTO.builder()
                .fullName("comtihon/test")
                .cloneUrl("https://github.com/comtihon/test.git")
                .versions(Collections.singletonList(new PackageVersionDTO("1.0.0")))
                .build();
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildSync", repo, ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=test", ResponseDTO.class);
        Assert.assertTrue(searchResponse.isResult());
        Assert.assertFalse(((List) searchResponse.getResponse()).isEmpty());
        waitLatch.await(10, TimeUnit.SECONDS);
        // search for dep
        ResponseDTO dep1Search =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=dep1", ResponseDTO.class);
        Assert.assertTrue(dep1Search.isResult());
        Assert.assertNotNull(dep1Search.getResponse());
        Assert.assertFalse(((List) dep1Search.getResponse()).isEmpty());
    }

    @Test
    public void testBuildWithAlreadyBuiltDep() throws IOException, InterruptedException {
        CountDownLatch waitLatch = waitForBuild(2);
        writeApp("test/tmp", "test", "1.0.0", CONF_WITH_DEP);
        writeApp("test/tmp", "dep1", "1.0.0", DEP_LEAF_CONF);
        //build dep
        RepositoryDTO repo = RepositoryDTO.builder()
                .fullName("comtihon/dep1")
                .cloneUrl("https://github.com/comtihon/dep1.git")
                .versions(Collections.singletonList(new PackageVersionDTO("1.0.0")))
                .build();
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildSync", repo, ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        Build depBuild = daoService.findSuccessfulBy(
                "github.com/comtihon/dep1",
                "1.0.0",
                erlangVersion);
        Assert.assertNotNull(depBuild);

        //build parent
        repo = RepositoryDTO.builder()
                .fullName("comtihon/test")
                .cloneUrl("https://github.com/comtihon/test.git")
                .versions(Collections.singletonList(new PackageVersionDTO("1.0.0")))
                .build();
        responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildSync", repo, ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        waitLatch.await(10, TimeUnit.SECONDS);
        Thread.sleep(400); //wait for false positives
        //build clean up (and build) was called only for dep and parent
        verify(gitService, times(2)).getClonedPaths(any(), any());
    }

    @Test
    public void testBuildWithMultipleDeps() throws IOException, InterruptedException {
        CountDownLatch waitLatch = waitForBuild(3);
        writeApp("test/tmp", "test", "1.0.0", CONF_WITH_DEP);
        writeApp("test/tmp", "dep1", "1.0.0", DEP_TREE_CONF);
        writeApp("test/tmp", "dep2", "1.0.0", DEP_LEAF_CONF);
        RepositoryDTO repo = RepositoryDTO.builder()
                .fullName("comtihon/test")
                .cloneUrl("https://github.com/comtihon/test.git")
                .versions(Collections.singletonList(new PackageVersionDTO("1.0.0")))
                .build();
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildSync", repo, ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=test", ResponseDTO.class);
        Assert.assertTrue(searchResponse.isResult());
        Assert.assertFalse(((List) searchResponse.getResponse()).isEmpty());
        waitLatch.await(10, TimeUnit.SECONDS);
        // search for dep
        ResponseDTO dep1Search =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=dep1", ResponseDTO.class);
        Assert.assertTrue(dep1Search.isResult());
        Assert.assertNotNull(dep1Search.getResponse());
        Assert.assertFalse(((List) dep1Search.getResponse()).isEmpty());
        // search for dep
        ResponseDTO dep2Search =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=dep2", ResponseDTO.class);
        Assert.assertTrue(dep2Search.isResult());
        Assert.assertNotNull(dep2Search.getResponse());
        Assert.assertFalse(((List) dep2Search.getResponse()).isEmpty());
        Thread.sleep(400); //wait for false positives
        verify(gitService, times(3)).getClonedPaths(any(), any());
    }

    @Test
    public void testBuildWithDepsWithDeps() {

    }

    /**
     * gitService.getClonedPaths is called after every build - for cleanup
     *
     * @param n number of calls
     * @return
     */
    private CountDownLatch waitForBuild(int n) {
        CountDownLatch waitLatch = new CountDownLatch(n);
        doAnswer((Answer<List<Path>>) invocation -> {
            waitLatch.countDown();
            return new ArrayList<>();
        }).when(gitService).getClonedPaths(any(), any());
        return waitLatch;
    }
}
