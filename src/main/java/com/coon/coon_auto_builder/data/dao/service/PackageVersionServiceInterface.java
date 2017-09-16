package com.coon.coon_auto_builder.data.dao.service;

import com.coon.coon_auto_builder.data.dao.PackageVersion;

import java.util.Optional;

public interface PackageVersionServiceInterface {
    PackageVersion saveVersion(PackageVersion pack);

    void deleteVersion(String vsnId);

    Optional<PackageVersion> findVersion(String vsnId);

    Optional<PackageVersion> findVersionByValues(String ref, String erlVsn, String url);
}
