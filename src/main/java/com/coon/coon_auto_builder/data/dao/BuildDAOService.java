package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.BuildBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
public class BuildDAOService {
    @Autowired
    BuildDAO dao;

    @Autowired
    PackageVersionDAOService packageVersionDAOService;

    //TODO try to find a way to save references automatically with CascadeType
    @Transactional
    public void save(BuildBO pack) {
        packageVersionDAOService.saveIfNotExists(pack.getPackageVersion());
        BuildBO saved = dao.save(pack);
        if (saved != null) {
            pack.setBuildId(saved.getBuildId());
        }
    }

    public Optional<BuildBO> findByValues(String name, String namespace, String ref, String erl) {
        return Optional.ofNullable(dao.findByNameAndNamespaceAndRefAndErl(name, namespace, ref, erl));
    }

    public Optional<BuildBO> find(String resId) {
        return Optional.ofNullable(dao.findOne(resId));
    }

    public Collection<BuildBO> getAll() {
        Iterable<BuildBO> itr = dao.findAll();
        return (Collection<BuildBO>) itr;
    }
}
