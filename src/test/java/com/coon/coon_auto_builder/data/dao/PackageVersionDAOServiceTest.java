package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.Build;
import com.coon.coon_auto_builder.data.entity.PackageVersion;
import com.coon.coon_auto_builder.data.entity.Repository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@RunWith(SpringRunner.class)
@DataJpaTest
@Transactional
public class PackageVersionDAOServiceTest {

    @Autowired
    private PackageVersionDAO packageVersionDAO;

    @Autowired
    private BuildDAO buildDAO;

    @Test
    public void save() throws Exception {
        PackageVersion pv = new PackageVersion("1.0.0", "18");
        packageVersionDAO.save(pv);
        Assert.assertNotNull(pv.getVersionId());
        PackageVersion find = packageVersionDAO.findOne(pv.getVersionId());
        Assert.assertNotNull(find);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void findByValues() throws Exception {
        PackageVersion found1Id =
                packageVersionDAO.findByRefAndErlVersionAndRepository("1.0.0", "18", "url1");
        Assert.assertNotNull(found1Id);
    }

    @Test
    public void addBuild() {
        PackageVersion pv = new PackageVersion("1.0.0", "18");
        packageVersionDAO.save(pv);
        pv.addBuild(new Build());
        pv.addBuild(new Build());
        packageVersionDAO.save(pv);
        Iterable<Build> itr = buildDAO.findAll();
        Assert.assertEquals(2, ((Collection<Build>) itr).size());
        packageVersionDAO.delete(pv.getVersionId());
        itr = buildDAO.findAll();
        Assert.assertEquals(0, ((Collection<Build>) itr).size());
    }
}