package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.BuildBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BuildDAOService implements DaoService<BuildBO> {
    @Autowired
    BuildDAO dao;

    @Autowired
    PackageVersionDAOService packageVersionDAOService;

    @Transactional
    public void save(BuildBO pack) {
        packageVersionDAOService.saveIfNotExists(pack.getPackageVersion());
        BuildBO saved = dao.save(pack);
        if (saved != null) {
            pack.setBuildId(saved.getBuildId());
        }
    }

    public Optional<BuildBO> findByValues(String name, String namespace, String ref, String erl) {
        List<BuildBO> builds = dao.findSuccessfullByNameAndNamespaceAndRefAndErl(name, namespace, ref, erl);
        if (builds.size() == 0) {
            return Optional.empty();
        } else if (builds.size() == 1) {
            return Optional.of(builds.get(0));
        } else {
            builds.sort(Comparator.comparing(BuildBO::getCreatedDate));
            return Optional.of(builds.get(builds.size()));
        }
    }

    public List<BuildBO> fetchByValues(String name, String namespace, String ref, String erl) {
        if (ref == null) return dao.findSuccessfullByNameAndNamespace(name, namespace);
        if (erl == null) return dao.findSuccessfullByNameAndNamespaceAndRef(name, namespace, ref);
        return dao.findSuccessfullByNameAndNamespaceAndRefAndErl(name, namespace, ref, erl);
    }

    @Override
    public Optional<BuildBO> findByNameAndNamespace(String name, String namespace) {
        return Optional.empty();
    }

    @Override
    public Optional<BuildBO> find(String buildId) {
        return Optional.ofNullable(dao.findOne(buildId));
    }

    @Override
    public void delete(String id) {
        dao.delete(id);
    }

    public Collection<BuildBO> getAll() {
        Iterable<BuildBO> itr = dao.findAll();
        return (Collection<BuildBO>) itr;
    }
}
