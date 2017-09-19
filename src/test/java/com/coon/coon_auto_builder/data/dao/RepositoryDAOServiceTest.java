package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.HibernateTestConfig;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
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

    @Test
    public void save() throws Exception {
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", "1.0.0", "url");
        Assert.notNull(repositoryDAO.save(repo), "saved instance should not be null");
        Optional<RepositoryBO> find = repositoryDAO.find("url");
        Assert.isTrue(find.isPresent(), "Repo should be found");
    }

    @Test
    public void saveExistent() throws Exception {
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", "1.0.0", "url");
        Assert.notNull(repositoryDAO.save(repo), "saved instance should not be null");
        repo = new RepositoryBO("path", "comtihon/coon", "1.1.0", "url");
        Assert.notNull(repositoryDAO.save(repo), "saved instance should not be null");
        Collection<RepositoryBO> repos = repositoryDAO.getAll();
        Assert.isTrue(1 == repos.size(), "should be only one repo per path");
    }
}