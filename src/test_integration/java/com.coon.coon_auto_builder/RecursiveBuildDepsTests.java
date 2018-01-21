package com.coon.coon_auto_builder;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@Slf4j
public class RecursiveBuildDepsTests extends IntegrationTest {
    private static final String CONF_WITH_DEPS =
            "{\"name\":\"test\",\"fullname\":\"comtihon/test\",\"app_vsn\":\"1.0.0\",\"deps\":[" +
                    "{\"name\":\"dep1\",\"url\":\"https://github.com/comtihon/dep1_url\",\"tag\":\"1.0.0\"}," +
                    "{\"name\":\"dep2\",\"url\":\"https://github.com/comtihon/dep2_url\",\"branch\":\"master\"}," +
                    "{\"name\":\"dep3\",\"url\":\"https://github.com/comtihon/dep3_url\",\"tag\":\"1.0.0\"}" +
                    "]}";
    private static final String CONF_WITH_DEP =
            "{\"name\":\"test\",\"fullname\":\"comtihon/test\",\"app_vsn\":\"1.0.0\",\"deps\":[" +
                    "{\"name\":\"dep1\",\"url\":\"https://github.com/comtihon/dep1.git\",\"tag\":\"1.0.0\"}" +
                    "]}";

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

    @Before
    public void setUp() throws Exception {
        super.setUp(gitService, coon, kerl, loader, erlangVersion, startSearch, mailSender);
    }

    @Test
    public void testBuildWithDeps() throws IOException, InterruptedException {
        writeApp("test/tmp", "test", "1.0.0", CONF_WITH_DEP);
        startSearch = new CountDownLatch(1);
        RepositoryDTO repo = new RepositoryDTO("comtihon/test",
                "https://github.com/comtihon/test.git",
                new PackageVersionDTO("1.0.0"));
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/buildSync", repo, ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        startSearch.await(30, TimeUnit.SECONDS);
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=test", ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        //TODO wait for async deps build
    }
}
