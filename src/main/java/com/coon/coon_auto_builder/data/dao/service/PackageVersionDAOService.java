package com.coon.coon_auto_builder.data.dao.service;

import com.coon.coon_auto_builder.data.dao.PackageVersionDAO;
import com.coon.coon_auto_builder.data.model.PackageVersionBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class PackageVersionDAOService {
    @Autowired
    PackageVersionDAO dao;

    public PackageVersionBO save(PackageVersionBO pack) {
        return dao.save(pack);
    }

    public Optional<PackageVersionBO> find(String resId) {
        return Optional.ofNullable(dao.findOne(resId));
    }

    public Collection<PackageVersionBO> getAll() {
        Iterable<PackageVersionBO> itr = dao.findAll();
        return (Collection<PackageVersionBO>) itr;
    }

    public Optional<PackageVersionBO> findByRefAndErlVersionAndRepositoryUrl(PackageVersionBO versionBO) {
        return Optional.ofNullable(dao.findByRefAndErlVersionAndRepositoryUrl(
                versionBO.getRef(),
                versionBO.getErlVsn(),
                versionBO.getRepository().getUrl()));
    }
}
