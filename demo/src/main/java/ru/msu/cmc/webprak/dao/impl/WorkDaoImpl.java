package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.WorkDao;
import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;

@Repository
public class WorkDaoImpl extends BaseDaoImpl<Work, Integer> implements WorkDao {

    public WorkDaoImpl() {
        super(Work.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Work> findByTitle(String title) {
        return entityManager
                .createQuery("select work from Work work where work.title = :title", Work.class)
                .setParameter("title", title)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Work> findByAuthor(Author author) {
        return entityManager
                .createQuery("select work from Work work join work.authors author where author = :author", Work.class)
                .setParameter("author", author)
                .getResultList();
    }
}
