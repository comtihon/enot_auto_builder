package com.coon.coon_auto_builder.loader;

import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.eclipse.jgit.annotations.Nullable;

public interface Loader {

    @Nullable
    RepositoryBO loadArtifacts(RepositoryBO repositoryBO);
}
