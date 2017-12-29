package com.coon.coon_auto_builder.data.dao;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface QueryDSLRepository<T> {
    List<T> findBy(BooleanExpression where);

    List<T> findBy(BooleanExpression where, StringPath groupBy);

    T findOneBy(BooleanExpression where);

    List<T> findLimit(BooleanExpression where, StringPath groupBy, int limit);
}
