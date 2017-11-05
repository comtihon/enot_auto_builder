package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.PackageVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
@Transactional
public class BuildDAOServiceTest {

    @Autowired
    private BuildDAO buildDAO;

    @Test
    public void save() throws Exception {
        PackageVersion pv = new PackageVersion("1.0.0", "18");
        Build build = new Build(pv, true, "artifact");
        buildDAO.save(build);
        Build findBuild = buildDAO.findOne(build.getBuildId());
        Assert.assertNotNull(findBuild);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void fetchByValues() throws Exception {
        List<Build> found = buildDAO.findSuccessfullByNameAndNamespaceAndRefAndErl(
                "name1", "namespace1", "1.0.0", "18");
        found.sort(Comparator.comparing(Build::getBuildId));
        Assert.assertEquals("build_id1", found.get(0).getBuildId());
        Assert.assertEquals("build_id2", found.get(1).getBuildId());
        Assert.assertEquals(2, found.size());

        found = buildDAO.findSuccessfullByNameAndNamespaceAndRef("name1", "namespace1", "1.0.0");
        found.sort(Comparator.comparing(Build::getBuildId));
        Assert.assertEquals("build_id1", found.get(0).getBuildId());
        Assert.assertEquals("build_id2", found.get(1).getBuildId());
        Assert.assertEquals("build_id3", found.get(2).getBuildId());
        Assert.assertEquals("build_id4", found.get(3).getBuildId());
        Assert.assertEquals(4, found.size());

        found = buildDAO.findSuccessfullByNameAndNamespace("name1", "namespace1");
        found.sort(Comparator.comparing(Build::getBuildId));
        Assert.assertEquals("build_id1", found.get(0).getBuildId());
        Assert.assertEquals("build_id2", found.get(1).getBuildId());
        Assert.assertEquals("build_id3", found.get(2).getBuildId());
        Assert.assertEquals("build_id4", found.get(3).getBuildId());
        Assert.assertEquals("build_id5", found.get(4).getBuildId());
        Assert.assertEquals(5, found.size());

        found = buildDAO.findSuccessfullByNameAndNamespace("name2", "namespace2");
        Assert.assertEquals("build_id6", found.get(0).getBuildId());
        Assert.assertEquals(1, found.size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:builds.sql")
    public void searchByName() {
        List<Build> found = buildDAO.findByName("ba");
        Assert.assertEquals(3, found.size());
        found.sort(Comparator.comparing(Build::getBuildId));
        Assert.assertEquals("build_id2", found.get(0).getBuildId());
        Assert.assertEquals("build_id3", found.get(1).getBuildId());
        Assert.assertEquals("build_id5", found.get(2).getBuildId());
        found = buildDAO.findByName("bar");
        Assert.assertEquals(2, found.size());
        found.sort(Comparator.comparing(Build::getBuildId));
        Assert.assertEquals("build_id2", found.get(0).getBuildId());
        Assert.assertEquals("build_id5", found.get(1).getBuildId());
    }
}