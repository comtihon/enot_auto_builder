package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.BuildBO;
import org.springframework.data.repository.CrudRepository;

public interface BuildDAO extends CrudRepository<BuildBO, String> {
}
