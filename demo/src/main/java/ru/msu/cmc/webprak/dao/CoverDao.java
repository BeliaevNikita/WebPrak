package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Cover;

import java.util.Optional;

public interface CoverDao extends BaseDao<Cover, Integer> {

    Optional<Cover> findByCoverName(String coverName);
}
