package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;

public interface EditionDao extends BaseDao<Edition, Integer> {

    List<Edition> findByWork(Work work);

    List<Edition> findAvailable();
}
