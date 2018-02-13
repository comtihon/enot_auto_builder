package com.enot.enot_auto_builder.service.build;

import com.enot.enot_auto_builder.data.dao.BuildDAOService;
import com.enot.enot_auto_builder.data.dto.RepositoryDTO;
import com.enot.enot_auto_builder.data.entity.Build;
import com.enot.enot_auto_builder.data.entity.PackageVersion;
import com.enot.enot_auto_builder.service.git.ClonedRepo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "non-async")
public class BuildServiceTest {

    @MockBean
    private BuildDAOService daoService;

    @Autowired
    private BuildService buildService;

    private static List<RepositoryDTO> builds;

    @Before
    public void setUp() {
        builds = new ArrayList<>();
    }

    /**
     * Second build was unsuccessful - we should not build deps for it (19 erl vsn)
     */
    @Test
    public void testFilterSuccessfulBuilds() {
        when(daoService.findSuccessfulBy(any(), any(), any())).thenReturn(null);
        ClonedRepo clonedRepo = new ClonedRepo("", Paths.get("/")) {
            @Override
            public List<Dep> getDeps() {
                return Arrays.asList(Dep.builder().name("dep1").url("url1").tag("1").erlVsn(new HashSet<>()).build(),
                        Dep.builder().name("dep2").url("url2").tag("1").erlVsn(new HashSet<>()).build());
            }
        };
        List<Build> results = Arrays.asList(
                new Build(new PackageVersion("1", "18"), true, "/"),
                new Build(new PackageVersion("1", "19"), false, "/"));
        buildService.buildDepsAsync(clonedRepo, results, builds::add);
        Assert.assertEquals(2, builds.size()); // 2 deps to build
        Assert.assertEquals(1, builds.get(0).getVersions().size());
        Assert.assertEquals("18", builds.get(0).getVersions().get(0).getErlVersion());
        Assert.assertEquals(1, builds.get(1).getVersions().size());
        Assert.assertEquals("18", builds.get(1).getVersions().get(0).getErlVersion());
    }

    /**
     * Dep1 is tagged - only it will be built.
     */
    @Test
    public void testFilterTagged() {
        when(daoService.findSuccessfulBy(any(), any(), any())).thenReturn(null);
        ClonedRepo clonedRepo = new ClonedRepo("", Paths.get("/")) {
            @Override
            public List<Dep> getDeps() {
                return Arrays.asList(Dep.builder().name("dep1").url("url1").tag("1").erlVsn(new HashSet<>()).build(),
                        Dep.builder().name("dep2").url("url2").branch("master").erlVsn(new HashSet<>()).build());
            }
        };
        List<Build> results = Arrays.asList(
                new Build(new PackageVersion("1", "18"), true, "/"),
                new Build(new PackageVersion("1", "19"), false, "/"));
        buildService.buildDepsAsync(clonedRepo, results,  builds::add);
        Assert.assertEquals(1, builds.size()); // 1 dep to build
        Assert.assertEquals("dep1", builds.get(0).getFullName());
    }

    @Test
    public void testMapVersions() {
        when(daoService.findSuccessfulBy(any(), any(), any())).thenReturn(null);
        ClonedRepo clonedRepo = new ClonedRepo("", Paths.get("/")) {
            @Override
            public List<Dep> getDeps() {
                return Arrays.asList(Dep.builder().name("dep1").url("url1").tag("1").erlVsn(new HashSet<>()).build(),
                        Dep.builder().name("dep2").url("url2").tag("1").erlVsn(new HashSet<>()).build());
            }
        };
        List<Build> results = Arrays.asList(
                new Build(new PackageVersion("1", "18"), true, "/"),
                new Build(new PackageVersion("1", "19"), true, "/"));
        buildService.buildDepsAsync(clonedRepo, results, builds::add);
        Assert.assertEquals(2, builds.size()); // 2 deps to build
        Assert.assertEquals(2, builds.get(0).getVersions().size()); // 2 versions each
        Assert.assertEquals("18", builds.get(0).getVersions().get(0).getErlVersion());
        Assert.assertEquals("19", builds.get(0).getVersions().get(1).getErlVersion());
        Assert.assertEquals(2, builds.get(1).getVersions().size());
        Assert.assertEquals("18", builds.get(1).getVersions().get(0).getErlVersion());
        Assert.assertEquals("19", builds.get(1).getVersions().get(1).getErlVersion());
    }

    /**
     * Dep1 (url1) is already in our system. This build should trigger only Dep2 building.
     */
    @Test
    public void testFilterExisting() {
        doAnswer((Answer<Build>) invocation -> {
            String url = (String) invocation.getArguments()[0];
            if (url.equals("url1")) return new Build();
            else return null;
        }).when(daoService).findSuccessfulBy(any(), any(), any());
        ClonedRepo clonedRepo = new ClonedRepo("", Paths.get("/")) {
            @Override
            public List<Dep> getDeps() {
                return Arrays.asList(Dep.builder().name("dep1").url("url1").tag("1").erlVsn(new HashSet<>()).build(),
                        Dep.builder().name("dep2").url("url2").tag("1").erlVsn(new HashSet<>()).build());
            }
        };
        List<Build> results = Arrays.asList(
                new Build(new PackageVersion("1", "18"), true, "/"),
                new Build(new PackageVersion("1", "19"), false, "/"));
        buildService.buildDepsAsync(clonedRepo, results, builds::add);
        Assert.assertEquals(1, builds.size()); // 1 dep to build
        Assert.assertEquals("dep2", builds.get(0).getFullName());
    }
}