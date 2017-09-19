package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.HibernateTestConfig;
import com.coon.coon_auto_builder.data.model.BuildBO;
import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {HibernateTestConfig.class})
@Transactional
public class BuildDAOServiceTest {

    @Autowired
    PackageVersionDAOService packageVersionDAOService;

    @Autowired
    RepositoryDAOService repositoryDAOService;

    @Autowired
    BuildDAOService buildDAOService;

    @Test
    public void save() throws Exception {
        final String repoUrl = "url";
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", "1.0.0", repoUrl);
        PackageVersionBO pv = new PackageVersionBO("1.0.0", "18", repo);
        BuildBO build = new BuildBO(pv, "build falied");
        buildDAOService.save(build);
        Optional<BuildBO> findBuild = buildDAOService.find(build.getBuildId());
        Assert.isTrue(findBuild.isPresent(), "Build should be saved");
        Optional<PackageVersionBO> find = packageVersionDAOService.find(pv.getVersionId());
        Assert.isTrue(find.isPresent(), "Version should be saved with build");
        Optional<RepositoryBO> repoSearch = repositoryDAOService.find(repoUrl);
        Assert.isTrue(repoSearch.isPresent(), "Repo should be saved with version");
    }

    @Test
    public void saveMultipleBuilds() throws Exception {
        final String repoUrl = "url";
        final String ref = "1.1.0";
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", ref, repoUrl);
        PackageVersionBO pv = new PackageVersionBO(ref, "18", repo);
        BuildBO build = new BuildBO(pv, Paths.get("/path/to/artifact"));
        BuildBO buildOK = new BuildBO(pv, "build falied");
        buildDAOService.save(build);
        buildDAOService.save(buildOK);
        Optional<BuildBO> findBuild = buildDAOService.find(build.getBuildId());
        Assert.isTrue(findBuild.isPresent(), "Build should be saved");
        findBuild = buildDAOService.find(buildOK.getBuildId());
        Assert.isTrue(findBuild.isPresent(), "Build should be saved");
        Optional<PackageVersionBO> find = packageVersionDAOService.find(pv.getVersionId());
        Assert.isTrue(find.isPresent(), "Version should be saved with build");
        Optional<RepositoryBO> repoSearch = repositoryDAOService.find(repoUrl);
        Assert.isTrue(repoSearch.isPresent(), "Repo should be saved with version");
        Collection<RepositoryBO> repos = repositoryDAOService.getAll();
        Assert.isTrue(1 == repos.size(), "should be only one repo");
        Collection<PackageVersionBO> versions = packageVersionDAOService.getAll();
        Assert.isTrue(1 == versions.size(), "should be only one version");
        Collection<BuildBO> builds = buildDAOService.getAll();
        Assert.isTrue(2 == builds.size(), "should be 2 builds");
    }

    @Test
    public void saveDifferentBuilds() throws Exception {
        final String repoUrl = "url";
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", "1.0.0", repoUrl);
        PackageVersionBO pv1 = new PackageVersionBO("1.0.0", "18", repo);
        PackageVersionBO pv2 = new PackageVersionBO("1.0.0", "19", repo);
        BuildBO build1 = new BuildBO(pv1, Paths.get("/path/to/artifact"));
        BuildBO build2 = new BuildBO(pv2, Paths.get("/path/to/artifact"));
        buildDAOService.save(build1);
        buildDAOService.save(build2);
        Optional<BuildBO> findBuild = buildDAOService.find(build1.getBuildId());
        Assert.isTrue(findBuild.isPresent(), "Build should be saved");
        findBuild = buildDAOService.find(build2.getBuildId());
        Assert.isTrue(findBuild.isPresent(), "Build should be saved");
        Collection<BuildBO> builds = buildDAOService.getAll();
        Assert.isTrue(2 == builds.size(), "should be 2 builds");
        Collection<RepositoryBO> repos = repositoryDAOService.getAll();
        Assert.isTrue(1 == repos.size(), "should be only one repo");
        Collection<PackageVersionBO> versions = packageVersionDAOService.getAll();
        Assert.isTrue(2 == versions.size(), "should be two versions");
    }

    @Test
    public void searchBuildByValues() throws Exception {
        final String repoUrl = "url";
        final String ref = "1.2.0";
        RepositoryBO repo = new RepositoryBO("path", "comtihon/coon", ref, repoUrl);
        PackageVersionBO pv = new PackageVersionBO(ref, "18", repo);
        PackageVersionBO pvOther = new PackageVersionBO(ref, "19", repo);
        BuildBO build = new BuildBO(pv, "build failed");
        BuildBO build2 = new BuildBO(pvOther, Paths.get("some path"));
        buildDAOService.save(build);
        buildDAOService.save(build2);
        Optional<BuildBO> maybeBuild = buildDAOService.findByValues(
                "coon", "comtihon", ref, "18");
        Assert.isTrue(!maybeBuild.isPresent(), "No successful builds should be found");
        BuildBO build3 = new BuildBO(pv, Paths.get("artifact"));
        buildDAOService.save(build3);
        maybeBuild = buildDAOService.findByValues(
                "coon", "comtihon", ref, "18");
        Assert.isTrue(maybeBuild.isPresent(), "Successful build should be found");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:populate_builds.sql")
    public void fetchByValues() throws Exception {
        List<BuildBO> found = buildDAOService.fetchByValues(
                "name1", "namespace1", "1.0.0", "18");
        found.sort(Comparator.comparing(BuildBO::getBuildId));
        Assert.isTrue(found.get(0).getBuildId().equals("build_id1"), "found first build");
        Assert.isTrue(found.get(1).getBuildId().equals("build_id2"), "found second build");
        Assert.isTrue(found.size() == 2, "Only two builds should be found");

        found = buildDAOService.fetchByValues("name1", "namespace1", "1.0.0", null);
        found.sort(Comparator.comparing(BuildBO::getBuildId));
        Assert.isTrue(found.get(0).getBuildId().equals("build_id1"), "found first build");
        Assert.isTrue(found.get(1).getBuildId().equals("build_id2"), "found second build");
        Assert.isTrue(found.get(2).getBuildId().equals("build_id3"), "found third build");
        Assert.isTrue(found.get(3).getBuildId().equals("build_id4"), "found fourth build");
        Assert.isTrue(found.size() == 4, "Only two builds should be found");

        found = buildDAOService.fetchByValues("name1", "namespace1", null, null);
        found.sort(Comparator.comparing(BuildBO::getBuildId));
        Assert.isTrue(found.get(0).getBuildId().equals("build_id1"), "found first build");
        Assert.isTrue(found.get(1).getBuildId().equals("build_id2"), "found second build");
        Assert.isTrue(found.get(2).getBuildId().equals("build_id3"), "found third build");
        Assert.isTrue(found.get(3).getBuildId().equals("build_id4"), "found fourth build");
        Assert.isTrue(found.get(4).getBuildId().equals("build_id5"), "found fifth build");
        Assert.isTrue(found.size() == 5, "Only two builds should be found");

        found = buildDAOService.fetchByValues("name2", "namespace2", null, null);
        Assert.isTrue(found.get(0).getBuildId().equals("build_id6"), "found first build");
        Assert.isTrue(found.size() == 1, "Only one build for second repository");
    }
}