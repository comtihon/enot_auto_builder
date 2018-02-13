package com.enot.enot_auto_builder.service.loader;

import com.enot.enot_auto_builder.service.build.Builder;

import java.io.IOException;

public interface Loader {

    String loadArtifact(Builder repositoryBO) throws IOException;
}
