package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.EditionDao;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;

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
    public List<Edition> findAvailable() {
        return entityManager
                .createQuery("select edition from Edition edition where edition.quantity > 0", Edition.class)
                .getResultList();
    }
}
