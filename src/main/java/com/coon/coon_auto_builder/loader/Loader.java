package com.coon.coon_auto_builder.loader;

import com.coon.coon_auto_builder.domain.ErlPackage;

import java.io.IOException;

public interface Loader {
    void loadArtifact(ErlPackage artifact) throws IOException;
}
