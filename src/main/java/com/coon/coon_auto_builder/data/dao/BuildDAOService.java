package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.Build;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BuildDAOService implements DaoService<Build> {
    @Autowired
    private BuildDAO dao;

    @Transactional
    public void save(Build pack) {
        Build saved = dao.save(pack);
        if (saved != null) {
            pack.setBuildId(saved.getBuildId());
        }
    }

    public Optional<Build> findByValues(String name, String namespace, String ref, String erl) {
        List<Build> builds = dao.findSuccessfullByNameAndNamespaceAndRefAndErl(name, namespace, ref, erl);
        if (builds.size() == 0) {
            return Optional.empty();
        } else if (builds.size() == 1) {
            return Optional.of(builds.get(0));
        } else {
            builds.sort(Comparator.comparing(Build::getCreatedDate));
            return Optional.of(builds.get(builds.size()));
        }
    }

    public List<Build> fetchByValues(String name, String namespace) {
        return fetchByValues(name, namespace, null, null);
    }

    public List<Build> fetchByValues(String name, String namespace, String ref, String erl) {
        if (ref == null) return dao.findSuccessfullByNameAndNamespace(name, namespace);
        if (erl == null) return dao.findSuccessfullByNameAndNamespaceAndRef(name, namespace, ref);
        return dao.findSuccessfullByNameAndNamespaceAndRefAndErl(name, namespace, ref, erl);
    }

    @Override
    public Optional<Build> findByNameAndNamespace(String name, String namespace) {
        return Optional.empty();
    }

    @Override
    public Optional<Build> find(String buildId) {
        return Optional.ofNullable(dao.findOne(buildId));
    }

    @Override
    public void delete(String id) {
        dao.delete(id);
    }

    public Collection<Build> getAll() {
        Iterable<Build> itr = dao.findAll();
        return (Collection<Build>) itr;
    }
}
