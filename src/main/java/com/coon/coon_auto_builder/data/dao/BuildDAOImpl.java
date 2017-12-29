package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.Build;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.coon.coon_auto_builder.data.entity.QBuild.build;
import static com.coon.coon_auto_builder.data.entity.QPackageVersion.packageVersion;
import static com.coon.coon_auto_builder.data.entity.QRepository.repository;
import static com.querydsl.core.group.GroupBy.groupBy;

public class BuildDAOImpl implements QueryDSLRepository<Build> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Build> findBy(BooleanExpression where) {
        return from(where).fetch();
    }

    @Override
    public List<Build> findBy(BooleanExpression where, StringPath groupBy) {
        Map results = from(where).transform(groupBy(groupBy).as(build));
        return new ArrayList<>(results.values());
    }

    @Override
    public Build findOneBy(BooleanExpression where) {
        return from(where).fetchFirst();
    }

    @Override
    public List<Build> findLimit(BooleanExpression where, StringPath groupBy, int limit) {
        Map results =  from(where).limit(limit).transform(groupBy(groupBy).as(build));
        return new ArrayList<>(results.values());
    }

    private JPAQuery<Build> from(BooleanExpression where) {
        JPAQuery<Build> query = new JPAQuery<>(em);
        query.from(build)
                .innerJoin(build.packageVersion, packageVersion)
                .innerJoin(build.packageVersion.repository, repository)
                .where(where)
                .orderBy(build.createdDate.desc());
        return query;
    }
}
