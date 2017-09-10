package com.coon.coon_auto_builder.jpa.service;

import com.coon.coon_auto_builder.domain.ErlPackage;
import com.coon.coon_auto_builder.repository.ErlPackageRepository;
import org.eclipse.jgit.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component("erlPackageServiceInterface")
public class ErlPackageServiceInterfaceImpl implements ErlPackageServiceInterface {

    @Autowired
    private ErlPackageRepository erlPackageRepository;


    @Override
    public ErlPackage savePackage(ErlPackage pack) {
        return erlPackageRepository.save(pack);
    }

    @Override
    public void deletePackage(String packId) {
        erlPackageRepository.delete(packId);
    }

    @Override
    public Optional<ErlPackage> findPackage(String packId) {
        return Optional.ofNullable(erlPackageRepository.findOne(packId));
    }

    @Override
    public List<ErlPackage> findByValues(String name, String namespace, String ref, String erlVsn) {
        if (erlVsn == null)
            return erlPackageRepository.findPackagesByNameAndNamespaceAndRef(name, namespace, ref);
        if (ref == null)
            return erlPackageRepository.findPackagesByNameAndNamespace(name, namespace);
        return erlPackageRepository.findPackagesByNameAndNamespaceAndRefAndErlVsn(name, namespace, ref, erlVsn);
    }

    @Override
    @Nullable
    public ErlPackage getByValues(String name, String namespace, String ref, String erlVsn) {
        List<ErlPackage> found = erlPackageRepository.findPackagesByNameAndNamespaceAndRefAndErlVsn(
                name, namespace, ref, erlVsn);
        if(found.isEmpty()) {
            return null;
        } else {
            return found.get(0);
        }
    }


    @Override
    public Collection<ErlPackage> getAllPackages() {
        Iterable<ErlPackage> itr = erlPackageRepository.findAll();
        return (Collection<ErlPackage>) itr;
    }
}
