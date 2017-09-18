package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PackageVersionDAO extends CrudRepository<PackageVersionBO, String> {
    @Query("SELECT pv FROM PackageVersionBO pv WHERE pv.ref = :ref and pv.erlVersion = :erl and repository_url = :url")
    PackageVersionBO findByRefAndErlVersionAndRepository(
            @Param("ref") String ref,
            @Param("erl") String erl,
            @Param("url") String url);
}
