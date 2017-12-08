package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.Build;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BuildDAOServiceTest {

    @Autowired
    private BuildDAOService buildDAOService;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:clean.sql")
    public void findBy() {
        List<Build> builds = buildDAOService.findBy("name1", "namespace1");
        Assert.assertEquals(5, builds.size());
        builds = buildDAOService.findBy("name1", "namespace1", "1.0.0", null, true);
        Assert.assertEquals(4, builds.size());
        builds = buildDAOService.findBy("name1", "namespace1", "1.0.0", "18", true);
        Assert.assertEquals(2, builds.size());
    }
}
