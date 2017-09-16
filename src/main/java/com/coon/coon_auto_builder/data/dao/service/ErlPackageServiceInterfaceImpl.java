package com.coon.coon_auto_builder.data.dao.service;

import com.coon.coon_auto_builder.data.dao.ErlPackage;
import com.coon.coon_auto_builder.data.dao.repository.ErlPackageRepository;
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
        return null;
    }

    @Override
    @Nullable
    public ErlPackage getByValues(String name, String namespace, String ref, String erlVsn) {
        return null;
    }


    @Override
    public Collection<ErlPackage> getAllPackages() {
        Iterable<ErlPackage> itr = erlPackageRepository.findAll();
        return (Collection<ErlPackage>) itr;
    }
}
