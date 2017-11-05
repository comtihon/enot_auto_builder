package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.entity.Build;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildDAO extends CrudRepository<Build, String> {
    @Query("SELECT b from Build b " +
            "join b.packageVersion pv " +
            "join pv.repository r " +
            "WHERE b.result = true " +
            "and r.name = :name " +
            "and r.namespace = :namespace " +
            "and pv.ref = :ref " +
            "and pv.erlVersion = :erl")
    List<Build> findSuccessfullByNameAndNamespaceAndRefAndErl(
            @Param("name") String name,
            @Param("namespace") String namespace,
            @Param("ref") String ref,
            @Param("erl") String erl);

    @Query("SELECT b from Build b " +
            "join b.packageVersion pv " +
            "join pv.repository r " +
            "WHERE b.result = true " +
            "and r.name = :name " +
            "and r.namespace = :namespace " +
            "and pv.ref = :ref")
    List<Build> findSuccessfullByNameAndNamespaceAndRef(
            @Param("name") String name,
            @Param("namespace") String namespace,
            @Param("ref") String ref);

    @Query("SELECT b from Build b " +
            "join b.packageVersion pv " +
            "join pv.repository r " +
            "WHERE b.result = true " +
            "and r.name = :name " +
            "and r.namespace = :namespace")
    List<Build> findSuccessfullByNameAndNamespace(
            @Param("name") String name,
            @Param("namespace") String namespace);
}
