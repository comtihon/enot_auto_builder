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
}