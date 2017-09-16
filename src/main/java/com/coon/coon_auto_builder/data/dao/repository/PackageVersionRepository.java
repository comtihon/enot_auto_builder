package com.coon.coon_auto_builder.data.dao.repository;

import com.coon.coon_auto_builder.data.dao.PackageVersion;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PackageVersionRepository extends CrudRepository<PackageVersion, String> {
    //TODO PackageVersion object as argument?
    PackageVersion findByErlVsnAndRefAndPackUrl(String erlVsn, String ref, String packUrl);
}
