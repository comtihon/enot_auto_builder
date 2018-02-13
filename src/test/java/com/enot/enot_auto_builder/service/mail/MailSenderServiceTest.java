package com.enot.enot_auto_builder.service.mail;

import com.enot.enot_auto_builder.data.dto.BuildDTO;
import com.enot.enot_auto_builder.data.entity.Build;
import com.enot.enot_auto_builder.data.entity.PackageVersion;
import com.enot.enot_auto_builder.data.entity.Repository;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MailSenderServiceTest {

    private MailSenderService senderService;

    @Before
    public void setUp() {
        senderService = new MailSenderService();
        senderService.setModelMapper(new ModelMapper());
    }

    @Test
    public void oneResult() {
        PackageVersion one = buildWithVersion("build_id", "email", "18", "1.0.0");
        Set<PackageVersion> versions = new HashSet<>();
        versions.add(one);
        new Repository("url", "testns/testn", versions);
        Map<String, MailReportDTO> reports = one.getBuildsRes().stream()
                .filter(build -> build.getPackageVersion().getEmail() != null)
                .collect(HashMap::new, senderService::addBuild, senderService::mergeBuilds);
        assertEquals(1, reports.size());
        MailReportDTO report = reports.get("email");
        assertNotNull(report);
        assertEquals(1, report.getResults().size());
        BuildDTO buildRes = report.getResults().get("18");
        assertNotNull(buildRes);
        assertEquals("build_id", buildRes.getBuildId());
    }

    @Test
    public void multipleErls() {
        PackageVersion one = buildWithVersion("build_id1", "email", "18", "1.0.0");
        PackageVersion two = buildWithVersion("build_id2", "email", "19", "1.0.0");
        Set<PackageVersion> versions = new HashSet<>();
        versions.add(one);
        versions.add(two);
        new Repository("url", "testns/testn", versions);
        List<Build> builds = one.getBuildsRes();
        builds.addAll(two.getBuildsRes());
        Map<String, MailReportDTO> reports = builds.stream()
                .filter(build -> build.getPackageVersion().getEmail() != null)
                .collect(HashMap::new, senderService::addBuild, senderService::mergeBuilds);
        assertEquals(1, reports.size());
        MailReportDTO report = reports.get("email");
        assertNotNull(report);
        assertEquals(2, report.getResults().size());
        BuildDTO buildRes = report.getResults().get("18");
        assertNotNull(buildRes);
        assertEquals("build_id1", buildRes.getBuildId());
        buildRes = report.getResults().get("19");
        assertNotNull(buildRes);
        assertEquals("build_id2", buildRes.getBuildId());
    }

    @Test
    public void multipleVersions() {
        PackageVersion one = buildWithVersion("build_id1", "emailA", "18", "1.0.0");
        PackageVersion two = buildWithVersion("build_id2", "emailB", "18", "1.1.0");
        Set<PackageVersion> versions = new HashSet<>();
        versions.add(one);
        versions.add(two);
        new Repository("url", "testns/testn", versions);
        List<Build> builds = one.getBuildsRes();
        builds.addAll(two.getBuildsRes());
        Map<String, MailReportDTO> reports = builds.stream()
                .filter(build -> build.getPackageVersion().getEmail() != null)
                .collect(HashMap::new, senderService::addBuild, senderService::mergeBuilds);
        assertEquals(2, reports.size());
        MailReportDTO report = reports.get("emailA");
        assertNotNull(report);
        assertEquals(1, report.getResults().size());
        BuildDTO buildRes = report.getResults().get("18");
        assertNotNull(buildRes);
        assertEquals("build_id1", buildRes.getBuildId());
        report = reports.get("emailB");
        assertEquals(1, report.getResults().size());
        buildRes = report.getResults().get("18");
        assertNotNull(buildRes);
        assertEquals("build_id2", buildRes.getBuildId());
    }

    @Test
    public void multipleErlsMultipleVersions() {
        PackageVersion oneA = buildWithVersion("build_id1", "emailA", "18", "1.0.0");
        PackageVersion twoA = buildWithVersion("build_id2", "emailA", "19", "1.0.0");
        PackageVersion oneB = buildWithVersion("build_id3", "emailB", "18", "1.1.0");
        PackageVersion twoB = buildWithVersion("build_id4", "emailB", "19", "1.1.0");
        Set<PackageVersion> versions = new HashSet<>();
        versions.add(oneA);
        versions.add(twoA);
        versions.add(oneB);
        versions.add(twoB);
        new Repository("url", "testns/testn", versions);
        List<Build> builds = oneA.getBuildsRes();
        builds.addAll(twoA.getBuildsRes());
        builds.addAll(oneB.getBuildsRes());
        builds.addAll(twoB.getBuildsRes());
        Map<String, MailReportDTO> reports = builds.stream()
                .filter(build -> build.getPackageVersion().getEmail() != null)
                .collect(HashMap::new, senderService::addBuild, senderService::mergeBuilds);
        assertEquals(2, reports.size());
        MailReportDTO report = reports.get("emailA");
        assertNotNull(report);
        assertEquals(2, report.getResults().size());
        BuildDTO buildRes = report.getResults().get("18");
        assertNotNull(buildRes);
        assertEquals("build_id1", buildRes.getBuildId());
        buildRes = report.getResults().get("19");
        assertNotNull(buildRes);
        assertEquals("build_id2", buildRes.getBuildId());
        report = reports.get("emailB");
        buildRes = report.getResults().get("18");
        assertNotNull(buildRes);
        assertEquals("build_id3", buildRes.getBuildId());
        buildRes = report.getResults().get("19");
        assertNotNull(buildRes);
        assertEquals("build_id4", buildRes.getBuildId());
    }

    private PackageVersion buildWithVersion(String id, String email, String erl, String ref) {
        PackageVersion one = new PackageVersion(ref, erl);
        one.setVersionId(Long.toHexString(Double.doubleToLongBits(Math.random())));
        one.setEmail(email);
        Build build = new Build(true, "/artifact.ep");
        build.setBuildId(id);
        one.addBuild(build);
        return one;
    }
}