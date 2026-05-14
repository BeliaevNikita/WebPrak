package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.EditionDao;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Work;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class EditionDaoImpl extends BaseDaoImpl<Edition, Integer> implements EditionDao {

    public EditionDaoImpl() {
        super(Edition.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> findByWork(Work work) {
        return entityManager
                .createQuery("select edition from Edition edition where edition.work = :work", Edition.class)
                .setParameter("work", work)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> findByWork(Work work, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select edition
                        from Edition edition
                        left join fetch edition.cover
                        where edition.work = :work
                        order by edition.publicationDate desc, edition.id desc
                        """, Edition.class)
                .setParameter("work", work)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> findAvailable() {
        return entityManager
                .createQuery("select edition from Edition edition where edition.quantity > 0", Edition.class)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> findAvailable(int offset, int limit) {
        return entityManager
                .createQuery("""
                        select edition
                        from Edition edition
                        left join fetch edition.work work
                        left join fetch work.authors
                        left join fetch edition.cover
                        where edition.quantity > 0
                        order by edition.id
                        """, Edition.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Edition> findByIdWithDetails(Integer id) {
        return entityManager
                .createQuery("""
                        select distinct edition
                        from Edition edition
                        left join fetch edition.work work
                        left join fetch work.authors
                        left join fetch edition.cover
                        where edition.id = :id
                        """, Edition.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> findPopular(int limit) {
        return entityManager
                .createQuery("""
                        select edition
                        from Edition edition
                        left join fetch edition.work work
                        left join fetch work.authors
                        left join fetch edition.cover
                        where edition.quantity > 0
                        order by (
                            select coalesce(sum(item.quantity), 0)
                            from OrderItem item
                            where item.edition = edition
                        ) desc, edition.id
                        """, Edition.class)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> searchByTitle(String titlePart, int offset, int limit) {
        return searchCatalog(titlePart, null, null, null, null, null, null, offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> searchByAuthor(String authorNamePart, int offset, int limit) {
        return searchCatalog(null, authorNamePart, null, null, null, null, null, offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, int offset, int limit) {
        return searchCatalog(null, null, minPrice, maxPrice, null, null, null, offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> findByPublicationDateBetween(LocalDate from, LocalDate to, int offset, int limit) {
        return searchCatalog(null, null, null, null, from, to, null, offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Edition> searchCatalog(String titlePart,
                                       String authorNamePart,
                                       BigDecimal minPrice,
                                       BigDecimal maxPrice,
                                       LocalDate publishedFrom,
                                       LocalDate publishedTo,
                                       Boolean onlyAvailable,
                                       int offset,
                                       int limit) {
        var query = entityManager.createQuery(buildCatalogQuery(Boolean.TRUE.equals(onlyAvailable)), Edition.class);
        setCatalogParameters(query, titlePart, authorNamePart, minPrice, maxPrice, publishedFrom, publishedTo);
        if (Boolean.TRUE.equals(onlyAvailable)) {
            query.setParameter("availableQuantity", 0);
        }
        return query.setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countCatalog(String titlePart,
                             String authorNamePart,
                             BigDecimal minPrice,
                             BigDecimal maxPrice,
                             LocalDate publishedFrom,
                             LocalDate publishedTo,
                             Boolean onlyAvailable) {
        var query = entityManager.createQuery(buildCatalogCountQuery(onlyAvailable), Long.class);
        setCatalogParameters(query, titlePart, authorNamePart, minPrice, maxPrice, publishedFrom, publishedTo);
        if (Boolean.TRUE.equals(onlyAvailable)) {
            query.setParameter("availableQuantity", 0);
        }
        return query.getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStock(Integer editionId, int quantity) {
        return entityManager
                .createQuery("""
                        select count(edition)
                        from Edition edition
                        where edition.id = :editionId and edition.quantity >= :quantity
                        """, Long.class)
                .setParameter("editionId", editionId)
                .setParameter("quantity", quantity)
                .getSingleResult() > 0;
    }

    @Override
    @Transactional
    public int updateStock(Integer editionId, int quantity) {
        int updated = entityManager
                .createQuery("""
                        update Edition edition
                        set edition.quantity = :quantity
                        where edition.id = :editionId
                        """)
                .setParameter("editionId", editionId)
                .setParameter("quantity", quantity)
                .executeUpdate();
        entityManager.clear();
        return updated;
    }

    @Override
    @Transactional
    public int incrementStock(Integer editionId, int delta) {
        int updated = entityManager
                .createQuery("""
                        update Edition edition
                        set edition.quantity = edition.quantity + :delta
                        where edition.id = :editionId
                        """)
                .setParameter("editionId", editionId)
                .setParameter("delta", delta)
                .executeUpdate();
        entityManager.clear();
        return updated;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String buildCatalogQuery(boolean onlyAvailable) {
        return """
                select distinct edition
                from Edition edition
                left join fetch edition.work work
                left join fetch work.authors author
                left join fetch edition.cover
                where 1 = 1
                """ + catalogPredicates(onlyAvailable) + " order by edition.id";
    }

    private String buildCatalogCountQuery(Boolean onlyAvailable) {
        return """
                select count(distinct edition)
                from Edition edition
                join edition.work work
                left join work.authors author
                where 1 = 1
                """ + catalogPredicates(Boolean.TRUE.equals(onlyAvailable));
    }

    private String catalogPredicates(boolean onlyAvailable) {
        StringBuilder jpql = new StringBuilder();
        jpql.append(" and (:titlePartValue = '' or lower(work.title) like :titlePart)");
        jpql.append(" and (:authorNamePartValue = '' or lower(author.firstName) like :authorNamePart or lower(author.lastName) like :authorNamePart)");
        jpql.append(" and (:minPriceValue = false or edition.price >= :minPrice)");
        jpql.append(" and (:maxPriceValue = false or edition.price <= :maxPrice)");
        jpql.append(" and (:publishedFromValue = false or edition.publicationDate >= :publishedFrom)");
        jpql.append(" and (:publishedToValue = false or edition.publicationDate <= :publishedTo)");
        if (onlyAvailable) {
            jpql.append(" and edition.quantity > :availableQuantity");
        }
        return jpql.toString();
    }

    private void setCatalogParameters(jakarta.persistence.Query query,
                                      String titlePart,
                                      String authorNamePart,
                                      BigDecimal minPrice,
                                      BigDecimal maxPrice,
                                      LocalDate publishedFrom,
                                      LocalDate publishedTo) {
        String normalizedTitle = blankToNull(titlePart);
        String normalizedAuthor = blankToNull(authorNamePart);
        query.setParameter("titlePartValue", normalizedTitle == null ? "" : normalizedTitle);
        query.setParameter("titlePart", normalizedTitle == null ? "" : "%" + normalizedTitle.toLowerCase() + "%");
        query.setParameter("authorNamePartValue", normalizedAuthor == null ? "" : normalizedAuthor);
        query.setParameter("authorNamePart", normalizedAuthor == null ? "" : "%" + normalizedAuthor.toLowerCase() + "%");
        query.setParameter("minPriceValue", minPrice != null);
        query.setParameter("minPrice", minPrice == null ? BigDecimal.ZERO : minPrice);
        query.setParameter("maxPriceValue", maxPrice != null);
        query.setParameter("maxPrice", maxPrice == null ? BigDecimal.ZERO : maxPrice);
        query.setParameter("publishedFromValue", publishedFrom != null);
        query.setParameter("publishedFrom", publishedFrom == null ? LocalDate.of(1, 1, 1) : publishedFrom);
        query.setParameter("publishedToValue", publishedTo != null);
        query.setParameter("publishedTo", publishedTo == null ? LocalDate.of(9999, 12, 31) : publishedTo);
    }
}
