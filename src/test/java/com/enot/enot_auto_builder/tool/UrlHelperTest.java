package com.enot.enot_auto_builder.tool;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class UrlHelperTest {

    @Test
    public void removeGitEnding() {
        assertEquals("http://githib.com/comtihon/enot",
                UrlHelper.removeGitEnding("http://githib.com/comtihon/enot.git"));
        assertEquals("http://githib.com/comtihon/enot",
                UrlHelper.removeGitEnding("http://githib.com/comtihon/enot"));
    }

    @Test
    public void removeProtocol() {
        assertEquals("githib.com/comtihon/enot",
                UrlHelper.removeProtocol("http://githib.com/comtihon/enot"));
        assertEquals("githib.com/comtihon/enot",
                UrlHelper.removeProtocol("git://githib.com/comtihon/enot"));
    }
}