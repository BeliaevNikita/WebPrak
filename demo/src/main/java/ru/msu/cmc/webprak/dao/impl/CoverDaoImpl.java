package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.CoverDao;
import ru.msu.cmc.webprak.entity.Cover;

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
}
