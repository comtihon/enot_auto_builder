package com.coon.coon_auto_builder.data.dao.service;

import com.coon.coon_auto_builder.data.dao.ErlPackage;
import org.eclipse.jgit.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ErlPackageServiceInterface {
    ErlPackage savePackage(ErlPackage pack);

    void deletePackage(String packId);

    Optional<ErlPackage> findPackage(String packId);

    List<ErlPackage> findByValues(String name, String namespace, String ref, String erlVsn);

    @Nullable
    ErlPackage getByValues(String name, String namespace, String ref, String erlVsn);

    Collection<ErlPackage> getAllPackages();
}
