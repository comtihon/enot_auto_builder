package com.coon.coon_auto_builder.tool;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class UrlHelperTest {

    @Test
    public void removeGitEnding() {
        Assert.assertEquals("http://githib.com/comtihon/coon",
                UrlHelper.removeGitEnding("http://githib.com/comtihon/coon.git"));
        Assert.assertEquals("http://githib.com/comtihon/coon",
                UrlHelper.removeGitEnding("http://githib.com/comtihon/coon"));
    }

    @Test
    public void removeProtocol() {
        Assert.assertEquals("githib.com/comtihon/coon",
                UrlHelper.removeProtocol("http://githib.com/comtihon/coon"));
        Assert.assertEquals("githib.com/comtihon/coon",
                UrlHelper.removeProtocol("git://githib.com/comtihon/coon"));
    }
}