package com.coon.coon_auto_builder.repository;


import com.coon.coon_auto_builder.domain.ErlPackage;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ErlPackageRepository extends CrudRepository<ErlPackage, String> {
    List<ErlPackage> findPackagesByNameAndNamespace(String name, String namespace);

    List<ErlPackage> findPackagesByNameAndNamespaceAndRef(String name, String namespace, String ref);

    List<ErlPackage> findPackagesByNameAndNamespaceAndRefAndErlVsn(String name, String namespace, String ref, String erlVsn);
}
