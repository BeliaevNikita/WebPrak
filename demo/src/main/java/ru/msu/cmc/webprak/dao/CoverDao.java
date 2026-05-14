package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Cover;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;
import java.util.Optional;

public interface CoverDao extends BaseDao<Cover, Integer> {

    Optional<Cover> findByCoverName(String coverName);

    List<Cover> findByWork(Work work);

    List<Cover> searchByCoverNameIgnoreCase(String coverNamePart);

    boolean existsByCoverName(String coverName);
}
