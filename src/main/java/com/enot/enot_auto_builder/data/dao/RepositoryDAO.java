package com.enot.enot_auto_builder.data.dao;

import com.enot.enot_auto_builder.data.entity.Repository;
import org.springframework.data.repository.CrudRepository;

@org.springframework.stereotype.Repository
public interface RepositoryDAO extends CrudRepository<Repository, String> {
    Repository findByNameAndNamespace(String name, String namespace);
}
