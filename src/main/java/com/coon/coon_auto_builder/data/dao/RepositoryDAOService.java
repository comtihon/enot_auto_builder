package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
public class RepositoryDAOService implements DaoService<RepositoryBO>{
    @Autowired
    RepositoryDAO dao;

    public RepositoryBO save(RepositoryBO repo) {
        return dao.save(repo);
    }

    @Transactional
    public RepositoryBO saveIfNotExists(RepositoryBO repo) {
        return find(repo.getUrl()).orElse(save(repo));
    }

    @Override
    public Optional<RepositoryBO> find(String resId) {
        return Optional.ofNullable(dao.findOne(resId));
    }

    @Override
    @Transactional
    public void delete(String id) {
        dao.delete(id);
    }

    @Override
    public Optional<RepositoryBO> findByNameAndNamespace(String name, String namespace) {
        return Optional.ofNullable(dao.findByNameAndNamespace(name, namespace));
    }

    public Collection<RepositoryBO> getAll() {
        Iterable<RepositoryBO> itr = dao.findAll();
        return (Collection<RepositoryBO>) itr;
    }
}
