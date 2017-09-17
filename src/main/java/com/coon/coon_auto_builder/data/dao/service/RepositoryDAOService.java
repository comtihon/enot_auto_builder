package com.coon.coon_auto_builder.data.dao.service;

import com.coon.coon_auto_builder.data.dao.RepositoryDAO;
import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class RepositoryDAOService {
    @Autowired
    RepositoryDAO dao;

    public RepositoryBO save(RepositoryBO pack) {
        return dao.save(pack);
    }

    public Optional<RepositoryBO> find(String resId) {
        return Optional.ofNullable(dao.findOne(resId));
    }

    public Collection<RepositoryBO> getAll() {
        Iterable<RepositoryBO> itr = dao.findAll();
        return (Collection<RepositoryBO>) itr;
    }
}
