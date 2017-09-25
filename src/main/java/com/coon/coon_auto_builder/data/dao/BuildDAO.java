package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.BuildBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BuildDAO extends CrudRepository<BuildBO, String> {
    //TODO select id only?
    @Query("SELECT b from BuildBO b " +
            "join b.packageVersion pv " +
            "join pv.repository r " +
            "WHERE b.result = true " +
            "and r.name = :name " +
            "and r.namespace = :namespace " +
            "and pv.ref = :ref " +
            "and pv.erlVersion = :erl")
    List<BuildBO> findSuccessfullByNameAndNamespaceAndRefAndErl(
            @Param("name") String name,
            @Param("namespace") String namespace,
            @Param("ref") String ref,
            @Param("erl") String erl);

    @Query("SELECT b from BuildBO b " +
            "join b.packageVersion pv " +
            "join pv.repository r " +
            "WHERE b.result = true " +
            "and r.name = :name " +
            "and r.namespace = :namespace " +
            "and pv.ref = :ref")
    List<BuildBO> findSuccessfullByNameAndNamespaceAndRef(
            @Param("name") String name,
            @Param("namespace") String namespace,
            @Param("ref") String ref);

    @Query("SELECT b from BuildBO b " +
            "join b.packageVersion pv " +
            "join pv.repository r " +
            "WHERE b.result = true " +
            "and r.name = :name " +
            "and r.namespace = :namespace")
    List<BuildBO> findSuccessfullByNameAndNamespace(
            @Param("name") String name,
            @Param("namespace") String namespace);
}
