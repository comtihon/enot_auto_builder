package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.RepositoryBO;
import org.springframework.data.repository.CrudRepository;

public interface RepositoryDAO extends CrudRepository<RepositoryBO, String> {
}
