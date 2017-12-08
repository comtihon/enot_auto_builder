package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.Build;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static com.coon.coon_auto_builder.data.entity.QBuild.build;
import static com.coon.coon_auto_builder.data.entity.QPackageVersion.packageVersion;
import static com.coon.coon_auto_builder.data.entity.QRepository.repository;

public class BuildDAOImpl implements QueryDSLRepository<Build> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Build> findBy(BooleanExpression where) {
        return from(where).fetch();
    }

    @Override
    public Build findOneBy(BooleanExpression where) {
        return from(where).fetchFirst();
    }

    private JPAQuery<Build> from(BooleanExpression where) {
        JPAQuery<Build> query = new JPAQuery<>(em);
        query.from(build)
                .innerJoin(build.packageVersion, packageVersion)
                .innerJoin(build.packageVersion.repository, repository).where(where);
        return query;
    }
}
