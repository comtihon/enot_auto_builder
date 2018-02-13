package com.enot.enot_auto_builder.data.dao;

import com.enot.enot_auto_builder.data.entity.Build;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.enot.enot_auto_builder.data.entity.QBuild.build;

@Service
public class BuildDAOService implements DaoService<Build> {
    @Autowired
    private BuildDAO dao;

    public Optional<Build> findBy(@NonNull String name, String namespace, String ref, String erl) {
        return Optional.ofNullable(dao.findOneBy(predicateFindBy(name, namespace, ref, erl, true)));
    }

    /**
     * Find all builds
     *
     * @param name      package name
     * @param namespace package namespace
     * @param ref       package ref
     * @param erl       Erlang version
     * @return list of builds
     */
    public List<Build> findBy(String name, String namespace, String ref, String erl, boolean onlySuccessful) {
        return dao.findBy(predicateFindBy(name, namespace, ref, erl, onlySuccessful));
    }

    public Build findSuccessfulBy(String url, String ref, String erl) {
        BooleanExpression expression = build.packageVersion.repository.url.eq(url)
                .and(build.packageVersion.ref.eq(ref))
                .and(build.packageVersion.erlVersion.eq(erl))
                .and(build.result.eq(true));
        return dao.findOneBy(expression);
    }

    public List<Build> findByGroupByPackage(String name, String namespace, String ref, String erl, boolean onlySuccessful) {
        return dao.findBy(predicateFindBy(name, namespace, ref, erl, onlySuccessful), build.artifactPath);
    }

    @Override
    public Optional<Build> findByNameAndNamespace(String name, String namespace) {
        return Optional.ofNullable(dao.findOneBy(predicateFindBy(name, namespace, null, null, false)));
    }

    @Override
    public Optional<Build> find(String buildId) {
        return Optional.ofNullable(dao.findOne(buildId));
    }

    @Override
    public void delete(String id) {
        dao.delete(id);
    }

    public Collection<Build> getAll() {
        Iterable<Build> itr = dao.findAll();
        return (Collection<Build>) itr;
    }

    public List<Build> getWithLimit(int n) {
        return dao.findLimit(build.result.eq(true), build.packageVersion.ref, n);
    }

    private BooleanExpression predicateFindBy(
            @NonNull String name, String namespace, String ref, String erl, boolean onlySuccessful) {
        BooleanExpression expression = build.packageVersion.repository.name.eq(name);
        if (onlySuccessful)
            expression = expression.and(build.result.eq(true));
        if (namespace != null && !namespace.isEmpty())
            expression = expression.and(build.packageVersion.repository.namespace.eq(namespace));
        if (ref != null && !ref.isEmpty())
            expression = expression.and(build.packageVersion.ref.eq(ref));
        if (erl != null && !erl.isEmpty())
            expression = expression.and(build.packageVersion.erlVersion.eq(erl));
        return expression;
    }
}
