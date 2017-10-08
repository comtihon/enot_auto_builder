package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.PackageVersion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageVersionDAO extends CrudRepository<PackageVersion, String> {
    @Query("SELECT pv FROM PackageVersion pv WHERE pv.ref = :ref and pv.erlVersion = :erl and repository_url = :url")
    PackageVersion findByRefAndErlVersionAndRepository(
            @Param("ref") String ref,
            @Param("erl") String erl,
            @Param("url") String url);
}
