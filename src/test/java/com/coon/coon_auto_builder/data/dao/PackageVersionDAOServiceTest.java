package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.HibernateTestConfig;
import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {HibernateTestConfig.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
public class PackageVersionDAOServiceTest {

    @Autowired
    PackageVersionDAOService packageVersionDAOService;

    @Autowired
    RepositoryDAOService repositoryDAOService;

    @Test
    public void save() throws Exception {
        final String repoUrl = "url";
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", "1.0.0", repoUrl);
        PackageVersionBO pv = new PackageVersionBO("1.0.0", "18", repo);
        packageVersionDAOService.save(pv);
        Assert.notNull(pv.getVersionId(), "saved id should not be null");
        Optional<PackageVersionBO> find = packageVersionDAOService.find(pv.getVersionId());
        Assert.isTrue(find.isPresent(), "Version should be found");
        Optional<RepositoryBO> repoSearch = repositoryDAOService.find(repoUrl);
        Assert.isTrue(repoSearch.isPresent(), "Repo should be saved with version");
    }

    @Test
    public void saveExistentRepo() throws Exception {
        final String repoUrl = "url";
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", "1.0.0", repoUrl);
        PackageVersionBO pv1 = new PackageVersionBO("1.0.0", "18", repo);
        PackageVersionBO pv2 = new PackageVersionBO("1.0.0", "19", repo);
        packageVersionDAOService.save(pv1);
        packageVersionDAOService.save(pv2);
        Collection<RepositoryBO> repos = repositoryDAOService.getAll();
        Assert.isTrue(1 == repos.size(), "should be only one repo per path");
    }

    @Test
    public void findByValues() throws Exception {
        final String repoUrl = "url";
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", "1.0.0", repoUrl);
        PackageVersionBO pv1 = new PackageVersionBO("1.0.0", "18", repo);
        PackageVersionBO pv2 = new PackageVersionBO("1.0.0", "19", repo);
        packageVersionDAOService.save(pv1);
        packageVersionDAOService.save(pv2);
        Optional<PackageVersionBO> found1 = packageVersionDAOService.findByRefAndErlVersionAndRepository(pv1);
        Assert.isTrue(found1.isPresent(), "vsn1 was found by values");
        Assert.isTrue(found1.get().getVersionId().equals(pv1.getVersionId()), "found version is pv1");
        Optional<PackageVersionBO> found2 = packageVersionDAOService.findByRefAndErlVersionAndRepository(pv2);
        Assert.isTrue(found2.isPresent(), "vsn2 was found by values");
        Assert.isTrue(found2.get().getVersionId().equals(pv2.getVersionId()), "found version is pv2");
    }
}