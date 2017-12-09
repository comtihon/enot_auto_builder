package com.coon.coon_auto_builder;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import com.coon.coon_auto_builder.data.dao.RepositoryDAOService;
import com.coon.coon_auto_builder.data.dto.PackageVersionDTO;
import com.coon.coon_auto_builder.data.dto.RepositoryDTO;
import com.coon.coon_auto_builder.service.GitService;
import com.coon.coon_auto_builder.service.MailSenderService;
import com.coon.coon_auto_builder.service.dto.CloneResult;
import com.coon.coon_auto_builder.service.loader.Loader;
import com.coon.coon_auto_builder.service.loader.LoaderFactory;
import com.coon.coon_auto_builder.service.tool.Coon;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class CoonAutoBuilderApplicationTests {

    private static final String NORMAL_CONF = "{\"name\":\"test\",\"fullname\":\"comtihon/test\",\"app_vsn\":\"1.0.0\"}";
    private static final String MULTIPLE_ERL_CONF =
            "{\"name\":\"test\",\"fullname\":\"comtihon/test\",\"app_vsn\":\"1.0.0\",\"erlang\":[\"18\",\"19\",\"20\"]}";

    @Value(value = "classpath:template.app.src")
    private Resource templateAppSrc;

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
        //mock repo clone
        Mockito.when(gitService.cloneRepo(
                any(),
                any(),
                any()))
                .thenReturn(new CloneResult("valerii.tikhonov@gmail.com", Paths.get("test/tmp/test")));
        Mockito.when(gitService.getClonedPaths(any(), any())).thenReturn(new ArrayList<>());
        //mock repo build
        Mockito.doNothing().when(coon).build(any(), any());
        //mock package loading
        Mockito.when(loaderFactory.createInstance()).thenReturn(repositoryBO -> "path/to/artifact/test.cp");
        Mockito.when(loader.loadArtifact(any())).thenReturn("path/to/artifact/test.cp");
        //mock email sending
        Mockito.doAnswer((Answer<Void>) invocation -> {
            startSearch.countDown();
            return null;
        }).when(mailSender).sendReport(any());
        //mock kerl
        Map<String, String> erlInstallations = new HashMap<>();
        erlInstallations.put("18", "path/to/18");
        erlInstallations.put("19", "path/to/19");
        erlInstallations.put("20", "path/to/20");
        erlInstallations.put(erlangVersion, "path/to/" + erlangVersion);
        Mockito.when(kerl.getErlInstallations()).thenReturn(erlInstallations);
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
        startSearch.await(10, TimeUnit.SECONDS);
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
        startSearch.await(20, TimeUnit.SECONDS);
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=test", ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        List<LinkedHashMap> packages = (List<LinkedHashMap>) searchResponse.getResponse();
        Assert.assertEquals(3, packages.size());
    }

    // Create application with name/coonfig.json and name/ebin/name.app
    private void writeApp(String pathStr, String name, String vsn, String conf) throws IOException {
        Paths.get(pathStr, name, "ebin").toFile().mkdirs();
        Path coonfig = Paths.get(pathStr, name, "coonfig.json");
        byte[] strToBytes = conf.getBytes();
        Files.write(coonfig, strToBytes);
        InputStream inputStream = templateAppSrc.getInputStream();
        String appFile = IOUtils.toString(inputStream, "UTF-8");
        appFile = appFile.replaceAll("#\\{name}", name);
        appFile = appFile.replaceAll("#\\{vsn}", vsn);
        Path appConf = Paths.get(pathStr, name, "ebin", name + ".app");
        strToBytes = appFile.getBytes();
        Files.write(appConf, strToBytes);
    }
}
