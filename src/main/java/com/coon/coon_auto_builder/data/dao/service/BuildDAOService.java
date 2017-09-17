package com.coon.coon_auto_builder.data.dao.service;

import com.coon.coon_auto_builder.data.dao.BuildDAO;
import com.coon.coon_auto_builder.data.model.BuildBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class BuildDAOService {
    @Autowired
    BuildDAO dao;

    public BuildBO save(BuildBO pack) {
        return dao.save(pack);
    }

    public Optional<BuildBO> find(String resId) {
        return Optional.ofNullable(dao.findOne(resId));
    }

    public Collection<BuildBO> getAll() {
        Iterable<BuildBO> itr = dao.findAll();
        return (Collection<BuildBO>) itr;
    }
}
