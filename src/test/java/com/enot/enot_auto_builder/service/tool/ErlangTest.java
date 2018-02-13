package com.enot.enot_auto_builder.service.tool;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ErlangTest {

    @Test
    public void ertsFindTest() throws IOException {
        Files.createDirectories(Paths.get("test/erts-9.0.1"));
        Files.createDirectories(Paths.get("test/bin"));
        Files.createDirectories(Paths.get("test/lib"));
        Files.createDirectories(Paths.get("test/doc"));
        Erlang erlang = new Erlang("test","test", "artifacts");
        Assert.assertEquals("test/erts-9.0.1", erlang.getErts().toString());
    }
}