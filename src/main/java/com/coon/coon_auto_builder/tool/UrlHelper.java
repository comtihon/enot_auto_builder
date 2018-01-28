package com.coon.coon_auto_builder.tool;

import org.apache.commons.io.FilenameUtils;

final public class UrlHelper {

    private UrlHelper() {

    }

    public static String removeGitEnding(String url) {
        if (url.endsWith(".git"))
            return FilenameUtils.removeExtension(url);
        else
            return url;
    }
}
