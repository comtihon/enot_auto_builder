package com.enot.enot_auto_builder.data.dao;

import com.enot.enot_auto_builder.data.entity.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Service
public class RepositoryDAOService implements DaoService<Repository> {
    @Autowired
    private RepositoryDAO dao;

    public Repository save(Repository repo) {
        return dao.save(repo);
    }

    public Repository getOrCreate(String url, String fullName) {
        return find(url).orElseGet(() -> save(new Repository(url, fullName, new HashSet<>())));
    }

    @Override
    public Optional<Repository> findByNameAndNamespace(String name, String namespace) {
        return Optional.ofNullable(dao.findByNameAndNamespace(name, namespace));
    }

    @Override
    public Optional<Repository> find(String resId) {
        return Optional.ofNullable(dao.findOne(resId));
    }

    @Override
    @Transactional
    public void delete(String id) {
        dao.delete(id);
    }

    public Collection<Repository> getAll() {
        Iterable<Repository> itr = dao.findAll();
        return (Collection<Repository>) itr;
    }
}
