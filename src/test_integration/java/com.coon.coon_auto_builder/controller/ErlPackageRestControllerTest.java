package com.coon.coon_auto_builder.controller;

import com.coon.coon_auto_builder.controller.dto.ResponseDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedHashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ErlPackageRestControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * In case of multiple builds for one version only one package/build should be shown with same path
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:multiple_builds.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:clean.sql")
    public void testMultipleBuildsSearch() {
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=name1", ResponseDTO.class);
        Assert.assertTrue(searchResponse.isResult());
        List<LinkedHashMap> packages = (List<LinkedHashMap>) searchResponse.getResponse();
        Assert.assertEquals(2, packages.size());
    }

    /**
     * Newer packages should be shown first
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:multiple_versions.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:clean.sql")
    public void testOrderedResultsSearch() {
        ResponseDTO searchResponse =
                this.restTemplate.getForObject(
                        "http://localhost:" + port + "/search?name=name1", ResponseDTO.class);
        Assert.assertTrue(searchResponse.isResult());
        List<LinkedHashMap> packages = (List<LinkedHashMap>) searchResponse.getResponse();
        Assert.assertEquals(4, packages.size());
        Assert.assertEquals("build_id4", packages.get(0).get("build_id"));
        Assert.assertEquals("build_id3", packages.get(1).get("build_id"));
        Assert.assertEquals("build_id2", packages.get(2).get("build_id"));
        Assert.assertEquals("build_id1", packages.get(3).get("build_id"));


    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:clean.sql")
    public void testBuildsFetch() {
        //TODO
    }
}