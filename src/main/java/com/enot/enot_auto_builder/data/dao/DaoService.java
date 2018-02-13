package com.enot.enot_auto_builder.data.dao;

import java.util.Optional;

public interface DaoService<T> {

    Optional<T> findByNameAndNamespace(String name, String namespace);

    Optional<T> find(String id);

    void delete(String id);
}
