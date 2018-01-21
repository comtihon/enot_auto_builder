package com.coon.coon_auto_builder.service.build;

import com.coon.coon_auto_builder.service.git.ClonedRepo;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class BuilderTest {

    private static final String NAME_OVERWRITE_CONF =
            "{\"name\":\"test_erl\",\"fullname\":\"comtihon/test\",\"app_vsn\":\"1.0.0\"}";
    private static final String ERLANG_APP =
            "{application, test, [\n" +
                    "  {description, \"desc\"},\n" +
                    "  {vsn, \"v1.0.0\"},\n" +
                    "  {registered, []},\n" +
                    "  {applications, [kernel, stdlib]}\n" +
                    "]}.";

    @Before
    public void setUp() {
        Paths.get("test/tmp/ebin").toFile().mkdirs();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File("test/tmp"));
    }

    /**
     * name from coonfig.json overrides erlang application name from .app file
     *
     * @throws IOException
     */
    @Test
    public void setPackageNameFromConf() throws IOException {
        writeFile("coonfig.json", NAME_OVERWRITE_CONF);
        writeFile("ebin/test.app", ERLANG_APP);
        Builder builder = new Builder(new ClonedRepo("", Paths.get("test/tmp")), "18");
        builder.withName("ns/other_name");
        builder.setBuildPath(Paths.get("test/tmp"));
        builder.detectPackageName(new HashMap<>());
        Assert.assertEquals("test_erl", builder.getPackageName());
    }

    /**
     * in case of no coonfig.json or no name in coonfig.json package name will be taken from .app file.
     *
     * @throws IOException
     */
    @Test
    public void setPackageNameFromApp() throws IOException {
        writeFile("ebin/test.app", ERLANG_APP);
        Builder builder = new Builder(new ClonedRepo("", Paths.get("test/tmp")), "18");
        builder.withName("ns/other_name");
        builder.setBuildPath(Paths.get("test/tmp"));
        builder.detectPackageName(new HashMap<>());
        Assert.assertEquals("test", builder.getPackageName());
    }

    /**
     * in case of no coonfig.json and no .app file - package name will be taken from name (Repository name)
     *
     */
    @Test
    public void setPackageNameFromName() {
        Builder builder = new Builder(new ClonedRepo("", Paths.get("test/tmp")), "18");
        builder.withName("ns/other_name");
        builder.setBuildPath(Paths.get("test/tmp"));
        builder.detectPackageName(new HashMap<>());
        Assert.assertEquals("other_name", builder.getPackageName());
    }

    private void writeFile(String filename, String content) throws IOException {
        Path coonfig = Paths.get("test/tmp", filename);
        byte[] strToBytes = content.getBytes();
        Files.write(coonfig, strToBytes);
    }

}