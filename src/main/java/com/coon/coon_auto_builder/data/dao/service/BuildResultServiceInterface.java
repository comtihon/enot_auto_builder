package com.coon.coon_auto_builder.data.dao.service;

import com.coon.coon_auto_builder.data.dao.BuildResult;
import org.eclipse.jgit.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BuildResultServiceInterface {
    BuildResult save(BuildResult pack);

    void delete(String packId);

    Optional<BuildResult> find(String resId);

    List<BuildResult> findByValues(String name, String namespace, String ref, String erlVsn);

    @Nullable
    BuildResult getByValues(String name, String namespace, String ref, String erlVsn);

    Collection<BuildResult> getAllBuildResults();
}
