package com.enot.enot_auto_builder.tool;

import org.junit.Test;

import static org.junit.Assert.*;

public class ErlangHelperTest {

    // from http://erlang.org/doc/man/app.html
    private final static String APP_CONF = "{application, my_application,\n" +
            "  [{description,  \"my_desc\"},\n" +
            "   {id,           \"1\"},\n" +
            "   {vsn,          \"1.0.0\"},\n" +
            "   {modules,      []},\n" +
            "   {maxP,         60},\n" +
            "   {maxT,         60},\n" +
            "   {registered,   [kernel, stdlib]},\n" +
            "   {included_applications, []},\n" +
            "   {applications, []},\n" +
            "   {env,          []},\n" +
            "   {mod,          []},\n" +
            "   {start_phases, []},\n" +
            "   {runtime_dependencies, []}]}.";

    private final static String APP_CONF_ATOM = "{application, 'my_application',\n" +
            "  [{description,  \"my_desc\"},\n" +
            "   {id,           \"1\"},\n" +
            "   {vsn,          \"1.0.0\"},\n" +
            "   {modules,      []},\n" +
            "   {maxP,         60},\n" +
            "   {maxT,         60},\n" +
            "   {registered,   [kernel, stdlib]},\n" +
            "   {included_applications, []},\n" +
            "   {applications, []},\n" +
            "   {env,          []},\n" +
            "   {mod,          []},\n" +
            "   {start_phases, []},\n" +
            "   {runtime_dependencies, []}]}.";

    @Test
    public void parseAppConfig() throws Exception {
        assertEquals("my_application", ErlangHelper.getName(APP_CONF));
        assertEquals("my_application", ErlangHelper.getName(APP_CONF_ATOM));
    }

}