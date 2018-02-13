package com.enot.enot_auto_builder.data.dao;

import com.enot.enot_auto_builder.data.entity.Build;
import com.enot.enot_auto_builder.data.entity.PackageVersion;
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
public class PackageVersionDAOServiceTest {

    @Autowired
    private PackageVersionDAOService packageVersionDAOService;

    @Autowired
    private BuildDAOService buildDAOService;

    @Test
    public void saveOne() {
        PackageVersion pv = packageVersionDAOService.getOrCreate("1.0.0", "18", "url");
        pv.addBuild(new Build(true, "path"));
        Assert.assertEquals(1, packageVersionDAOService.getAll().size());
        Assert.assertEquals(1, buildDAOService.getAll().size());
    }
}
