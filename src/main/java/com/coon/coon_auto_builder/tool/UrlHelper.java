package com.coon.coon_auto_builder.tool;

import org.apache.commons.io.FilenameUtils;

import java.util.regex.Pattern;

final public class UrlHelper {

    private UrlHelper() {

    }

    public static String removeGitEnding(String url) {
        if (url.endsWith(".git"))
            return FilenameUtils.removeExtension(url);
        else
            return url;
    }

    /**
     * @param url Ex. https://github.com/comtihon/bson-erlang
     * @return url without protocol. Ex. github.com/comtihon/bson-erlang
     */
    public static String removeProtocol(String url) {
        if(url.contains("://")) {
            String[] splitted = url.split(Pattern.quote("://"));
            return splitted[1];
        } else
            return url;
    }
}
