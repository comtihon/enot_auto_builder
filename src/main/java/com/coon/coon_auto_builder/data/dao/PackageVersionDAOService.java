package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.PackageVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class PackageVersionDAOService {
    @Autowired
    private PackageVersionDAO dao;

    public PackageVersion save(PackageVersion pack) {
        return dao.save(pack);
    }

    public PackageVersion getOrCreate(String ref, String erlVsn, String repoUrl) {
        return findByRefAndErlVersionAndRepository(ref, erlVsn, repoUrl)
                .orElseGet(() -> save(new PackageVersion(ref, erlVsn)));
    }

    public Optional<PackageVersion> find(String resId) {
        return Optional.ofNullable(dao.findOne(resId));
    }

    public Collection<PackageVersion> getAll() {
        Iterable<PackageVersion> itr = dao.findAll();
        return (Collection<PackageVersion>) itr;
    }

    public Optional<PackageVersion> findByRefAndErlVersionAndRepository(String ref, String erlVsn, String repoUrl) {
        return Optional.ofNullable(dao.findByRefAndErlVersionAndRepository(ref, erlVsn, repoUrl));
    }
}
