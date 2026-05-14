package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.CoverDao;
import ru.msu.cmc.webprak.entity.Cover;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;
import java.util.Optional;

@Repository
public class CoverDaoImpl extends BaseDaoImpl<Cover, Integer> implements CoverDao {

    public CoverDaoImpl() {
        super(Cover.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cover> findByCoverName(String coverName) {
        return entityManager
                .createQuery("select cover from Cover cover where cover.coverName = :coverName", Cover.class)
                .setParameter("coverName", coverName)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cover> findByWork(Work work) {
        return entityManager
                .createQuery("""
                        select distinct cover
                        from Edition edition join edition.cover cover
                        where edition.work = :work
                        order by cover.coverName
                        """, Cover.class)
                .setParameter("work", work)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cover> searchByCoverNameIgnoreCase(String coverNamePart) {
        return entityManager
                .createQuery("""
                        select cover
                        from Cover cover
                        where lower(cover.coverName) like lower(concat('%', :coverNamePart, '%'))
                        order by cover.coverName
                        """, Cover.class)
                .setParameter("coverNamePart", coverNamePart)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCoverName(String coverName) {
        return entityManager
                .createQuery("select count(cover) from Cover cover where cover.coverName = :coverName", Long.class)
                .setParameter("coverName", coverName)
                .getSingleResult() > 0;
    }
}
