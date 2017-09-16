package com.coon.coon_auto_builder.data.dao.service;

import com.coon.coon_auto_builder.data.dao.PackageVersion;
import com.coon.coon_auto_builder.data.dao.repository.PackageVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("packageVersionServiceInterface")
public class PackageVersionServiceInterfaceImpl implements PackageVersionServiceInterface {
    @Autowired
    private PackageVersionRepository packageVersionRepository;

    @Override
    public PackageVersion saveVersion(PackageVersion pack) {
        return packageVersionRepository.save(pack);
    }

    @Override
    public Optional<PackageVersion> findVersionByValues(String ref, String erlVsn, String url) {
        return Optional.ofNullable(packageVersionRepository.findByErlVsnAndRefAndPackUrl(erlVsn, ref, url));
    }

    @Override
    public void deleteVersion(String vsnId) {
        packageVersionRepository.delete(vsnId);
    }

    @Override
    public Optional<PackageVersion> findVersion(String vsnId) {
        return Optional.ofNullable(packageVersionRepository.findOne(vsnId));
    }
}
