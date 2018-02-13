package com.enot.enot_auto_builder.data.dao;

import com.enot.enot_auto_builder.data.entity.Build;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildDAO extends CrudRepository<Build, String>, QueryDSLRepository<Build> {
}
