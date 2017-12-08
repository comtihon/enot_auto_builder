package com.coon.coon_auto_builder.data.dao;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@NoRepositoryBean
public interface QueryDSLRepository<T> {
    List<T> findBy(BooleanExpression where);

    T findOneBy(BooleanExpression where);
}
