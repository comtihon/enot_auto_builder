package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RepositoryDAO extends CrudRepository<RepositoryBO, String> {
    RepositoryBO findByNameAndNamespace(String name, String namespace);
}
