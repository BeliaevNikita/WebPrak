package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.common.BaseEntity;

import java.util.List;
import java.util.Optional;

public interface BaseDao<T extends BaseEntity<ID>, ID> {

    Optional<T> getById(ID id);

    List<T> getAll();

    List<T> getAll(int offset, int limit);

    long count();

    boolean existsById(ID id);

    T save(T entity);

    T update(T entity);

    void delete(T entity);

    void deleteById(ID id);
}
