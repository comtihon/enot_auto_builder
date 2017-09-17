package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import org.springframework.data.repository.CrudRepository;

public interface PackageVersionDAO extends CrudRepository<PackageVersionBO, String> {
    PackageVersionBO findByRefAndErlVersionAndRepositoryUrl(String ref, String erl, String url);
}
