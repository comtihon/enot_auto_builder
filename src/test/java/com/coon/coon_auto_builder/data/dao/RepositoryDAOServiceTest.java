package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.HibernateTestConfig;
import com.coon.coon_auto_builder.data.model.BuildBO;
import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {HibernateTestConfig.class})
@Transactional
public class RepositoryDAOServiceTest {

    @Autowired
    RepositoryDAOService repositoryDAO;

    @Autowired
    PackageVersionDAOService packageVersionDAOService;

    @Autowired
    BuildDAOService buildDAOService;

    @Test
    public void save() throws Exception {
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", "1.0.0", "url", null);
        Assert.notNull(repositoryDAO.save(repo), "saved instance should not be null");
        Optional<RepositoryBO> find = repositoryDAO.find("url");
        Assert.isTrue(find.isPresent(), "Repo should be found");
    }

    @Test
    public void saveExistent() throws Exception {
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", "1.0.0", "url", null);
        Assert.notNull(repositoryDAO.save(repo), "saved instance should not be null");
        repo = new RepositoryBO("path", "comtihon/coon", "1.1.0", "url", null);
        Assert.notNull(repositoryDAO.save(repo), "saved instance should not be null");
        Collection<RepositoryBO> repos = repositoryDAO.getAll();
        Assert.isTrue(1 == repos.size(), "should be only one repo per path");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void cascadeDelete() throws Exception {
        final String url = "url1";
        Optional<RepositoryBO> find = repositoryDAO.find(url);
        Assert.isTrue(find.isPresent(), "Repo should be populated via populate_builds.sql");
        repositoryDAO.delete(url);

        find = repositoryDAO.find(url);
        Assert.isTrue(!find.isPresent(), "Repo should be deleted");

        Optional<PackageVersionBO> vsn = packageVersionDAOService.find("version_id1");
        Assert.isTrue(!vsn.isPresent(), "all versions of repo should be deleted");
        vsn = packageVersionDAOService.find("version_id2");
        Assert.isTrue(!vsn.isPresent(), "all versions of repo should be deleted");
        Optional<BuildBO> build = buildDAOService.find("build_id1");
        Assert.isTrue(!build.isPresent(), "Build should be deleted");
        build = buildDAOService.find("build_id2");
        Assert.isTrue(!build.isPresent(), "Build should be deleted");
    }
}