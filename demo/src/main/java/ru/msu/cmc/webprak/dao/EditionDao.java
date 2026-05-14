package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Work;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EditionDao extends BaseDao<Edition, Integer> {

    List<Edition> findByWork(Work work);

    List<Edition> findByWork(Work work, int offset, int limit);

    List<Edition> findAvailable();

    List<Edition> findAvailable(int offset, int limit);

    Optional<Edition> findByIdWithDetails(Integer id);

    List<Edition> findPopular(int limit);

    List<Edition> searchByTitle(String titlePart, int offset, int limit);

    List<Edition> searchByAuthor(String authorNamePart, int offset, int limit);

    List<Edition> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, int offset, int limit);

    List<Edition> findByPublicationDateBetween(LocalDate from, LocalDate to, int offset, int limit);

    List<Edition> searchCatalog(String titlePart,
                                String authorNamePart,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                LocalDate publishedFrom,
                                LocalDate publishedTo,
                                Boolean onlyAvailable,
                                int offset,
                                int limit);

    long countCatalog(String titlePart,
                      String authorNamePart,
                      BigDecimal minPrice,
                      BigDecimal maxPrice,
                      LocalDate publishedFrom,
                      LocalDate publishedTo,
                      Boolean onlyAvailable);

    boolean hasStock(Integer editionId, int quantity);

    int updateStock(Integer editionId, int quantity);

    int incrementStock(Integer editionId, int delta);
}
