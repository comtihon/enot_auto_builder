package com.enot.enot_auto_builder.data.dao;

import com.enot.enot_auto_builder.data.entity.Build;
import com.enot.enot_auto_builder.data.entity.PackageVersion;
import com.enot.enot_auto_builder.data.entity.Repository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class RepositoryDAOServiceTest {
    @Autowired
    private RepositoryDAOService repositoryDAOService;

    @Autowired
    private PackageVersionDAOService packageVersionDAOService;

    @Autowired
    private BuildDAOService buildDAOService;

    @Test
    public void saveSaveOne() {
        Repository repo = repositoryDAOService.getOrCreate("url", "comtihon/enot");
        repo.addVersion(new PackageVersion("1.0.0", "18"));
        Assert.assertEquals(1, repositoryDAOService.getAll().size());
        Assert.assertEquals(1, packageVersionDAOService.getAll().size());
    }

    @Test
    public void saveMultipleVersions() {
        Repository repo = repositoryDAOService.getOrCreate("url", "comtihon/enot");
        repo.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "18", "url"));
        repo.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "19", "url"));
        repo = repositoryDAOService.getOrCreate("url", "comtihon/enot");
        repo.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "20", "url"));
        System.out.println(packageVersionDAOService.getAll());
        Assert.assertEquals(1, repositoryDAOService.getAll().size());
        Assert.assertEquals(3, packageVersionDAOService.getAll().size());
    }

    @Test
    public void saveMultipleVersionsTwice() {
        Repository repo = repositoryDAOService.getOrCreate("url", "comtihon/enot");
        repo.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "18", "url"));
        repo.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "20", "url"));
        repo = repositoryDAOService.getOrCreate("url", "comtihon/enot");
        repo.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "20", "url"));
        Assert.assertEquals(1, repositoryDAOService.getAll().size());
        Assert.assertEquals(2, packageVersionDAOService.getAll().size());
    }

    @Test
    public void saveDifferentReposWithSameVersions() {
        Repository repo1 = repositoryDAOService.getOrCreate("url1", "comtihon/enot");
        Repository repo2 = repositoryDAOService.getOrCreate("url2", "comtihon/enot");
        repo1.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "18", "url1"));
        repo1.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "19", "url1"));
        repo2.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "18", "url2"));
        repo2.addVersion(packageVersionDAOService.getOrCreate("1.0.0", "19", "url2"));
        Assert.assertEquals(2, repositoryDAOService.getAll().size());
        Assert.assertEquals(4, packageVersionDAOService.getAll().size());
    }

    @Test
    public void resaveMultipleVersions() {
        Repository repo = repositoryDAOService.getOrCreate("url", "comtihon/enot");
        PackageVersion pv1 = packageVersionDAOService.getOrCreate("1.0.0", "18", "url");
        PackageVersion pv2 = packageVersionDAOService.getOrCreate("1.0.0", "19", "url");
        pv1.addBuild(new Build(true, "path"));
        pv2.addBuild(new Build(false, "error"));
        repo.addVersion(pv1);
        repo.addVersion(pv2);
        repo = repositoryDAOService.getOrCreate("url", "comtihon/enot");
        pv1 = packageVersionDAOService.getOrCreate("1.0.0", "18", "url");
        pv1.addBuild(new Build(true, "path"));
        repo.addVersion(pv1);
        Assert.assertEquals(1, repositoryDAOService.getAll().size());
        Assert.assertEquals(2, packageVersionDAOService.getAll().size());
        Assert.assertEquals(3, buildDAOService.getAll().size());
    }
}
