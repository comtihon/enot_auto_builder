package com.coon.coon_auto_builder.data.dao;

import com.coon.coon_auto_builder.data.model.BuildBO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface BuildDAO extends CrudRepository<BuildBO, String> {
    @Query("SELECT b from BuildBO b " +
            "join b.packageVersion pv " +
            "join pv.repository r " +
            "WHERE b.result = true " +
            "and r.name = :name " +
            "and r.namespace = :namespace " +
            "and pv.ref = :ref " +
            "and pv.erlVersion = :erl")
    BuildBO findByNameAndNamespaceAndRefAndErl(
            @Param("name") String name,
            @Param("namespace") String namespace,
            @Param("ref") String ref,
            @Param("erl") String erl);
}
