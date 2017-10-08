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
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
@Transactional
public class RepositoryDAOServiceTest {

    @Autowired
    private RepositoryDAO repositoryDAO;

    @Autowired
    private PackageVersionDAO packageVersionDAO;

    @Autowired
    private BuildDAO buildDAO;

    @Test
    public void save() throws Exception {
        List<PackageVersion> versions = Collections.singletonList(new PackageVersion("1.0.0", "18"));
        Repository repo = new Repository("url", "comtihon/coon", versions);
        Assert.assertNotNull(repositoryDAO.save(repo));
        Repository find = repositoryDAO.findOne("url");
        Assert.assertNotNull(find);
    }

    @Test
    public void saveExistent() throws Exception {
        List<PackageVersion> versions1 = Collections.singletonList(new PackageVersion("1.0.0", "18"));
        List<PackageVersion> versions2 = Collections.singletonList(new PackageVersion("1.1.0", "18"));
        Repository repo = new Repository("url", "comtihon/coon", versions1);
        Assert.assertNotNull(repositoryDAO.save(repo));
        repo = new Repository("url", "comtihon/coon", versions2);
        Assert.assertNotNull(repositoryDAO.save(repo));
        Iterable<Repository> itr = repositoryDAO.findAll();
        Assert.assertEquals(1, ((Collection<Repository>) itr).size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void cascadeDelete() throws Exception {
        final String url = "url1";
        Repository find = repositoryDAO.findOne(url);
        Assert.assertNotNull(find);
        repositoryDAO.delete(url);

        find = repositoryDAO.findOne(url);
        Assert.assertNull(find);

        PackageVersion vsn = packageVersionDAO.findOne("version_id1");
        Assert.assertNull(vsn);
        vsn = packageVersionDAO.findOne("version_id2");
        Assert.assertNull(vsn);
        Build build = buildDAO.findOne("build_id1");
        Assert.assertNull(build);
        build = buildDAO.findOne("build_id2");
        Assert.assertNull(build);
    }
}