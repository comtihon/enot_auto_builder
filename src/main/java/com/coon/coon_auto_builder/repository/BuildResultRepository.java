package com.coon.coon_auto_builder.repository;

import com.coon.coon_auto_builder.domain.BuildResult;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BuildResultRepository extends CrudRepository<BuildResult, String> {
    List<BuildResult> findByNameAndNamespace(String name, String namespace);

    List<BuildResult> findByNameAndNamespaceAndRef(String name, String namespace, String ref);

    List<BuildResult> findByNameAndNamespaceAndRefAndErlVsn(String name, String namespace, String ref, String erlVsn);
}
