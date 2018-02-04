package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dto.BuildDTO;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.service.git.CloneException;
import com.coon.coon_auto_builder.service.git.GitService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.mockito.Matchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ErlPackageDownloadControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @SpyBean
    private ErlPackageDownloadController controller;

    @Autowired
    private GitService service;

    @Before
    public void setUp() throws Exception {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            ResponseDTO result = (ResponseDTO) args[0];
            HttpServletResponse response = (HttpServletResponse) args[1];
            String out;
            if (result.isResult())
                out = ((BuildDTO) result.getResponse()).getArtifactPath();
            else
                out = (String) result.getResponse();
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(out.getBytes(Charset.forName("UTF-8")));
            outputStream.close();
            return null;
        }).when(controller).renderPackage(any(), any());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:multiple_builds.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:clean.sql")
    public void downloadBySearchName() {
        RepositoryDTO repo = RepositoryDTO.builder().fullName("namespace1/name1").cloneUrl("url").build();
        String response =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/get", repo, String.class);
        Assert.assertEquals("path", response);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:multiple_versions.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:clean.sql")
    public void downloadBySearchVersion() {
        RepositoryDTO repo = RepositoryDTO.builder().fullName("namespace1/name1").cloneUrl("url").build();
        repo.setVersions(Collections.singletonList(new PackageVersionDTO("1.0.1", "18")));
        String response =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/get", repo, String.class);
        Assert.assertEquals("1.0.1/path", response);
    }

    /**
     * Get list of builds, fetch build by ID
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:multiple_versions.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:clean.sql")
    public void downloadByListIds() {
        RepositoryDTO repo = RepositoryDTO.builder().fullName("namespace1/name1").cloneUrl("url").build();
        ResponseDTO buildsNoVersions =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/builds", repo, ResponseDTO.class);
        Assert.assertTrue(buildsNoVersions.isResult());
        List<LinkedHashMap> packages = (List<LinkedHashMap>) buildsNoVersions.getResponse();
        String buildId = (String) packages.get(1).get("build_id");
        String response = this.restTemplate.getForObject(
                "http://localhost:" + port + "/download/" + buildId, String.class);
        Assert.assertEquals("1.0.2/path", response);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:multiple_builds.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:clean.sql")
    public void getLogsErroredPackage() {
        String response = this.restTemplate.getForObject(
                "http://localhost:" + port + "/build_log?build_id=build_id1", String.class);
        Assert.assertEquals("something is wrong...", response);
    }

}