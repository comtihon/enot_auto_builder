package com.coon.coon_auto_builder.service.loader;

import com.coon.coon_auto_builder.service.build.Builder;

import java.io.IOException;

public interface Loader {

    String loadArtifact(Builder repositoryBO) throws IOException;
}
