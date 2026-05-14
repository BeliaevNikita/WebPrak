package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.WorkDao;
import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;
import java.util.Optional;

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
    public List<Work> searchByTitleIgnoreCase(String titlePart, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select work
                        from Work work
                        where lower(work.title) like lower(concat('%', :titlePart, '%'))
                        order by work.title
                        """, Work.class)
                .setParameter("titlePart", titlePart)
                .setFirstResult(offset)
                .setMaxResults(limit)
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

    @Override
    @Transactional(readOnly = true)
    public List<Work> findByAuthor(Author author, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select work
                        from Work work join work.authors author
                        where author = :author
                        order by work.title
                        """, Work.class)
                .setParameter("author", author)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Work> searchByAuthorNameIgnoreCase(String authorNamePart, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select distinct work
                        from Work work join work.authors author
                        where lower(author.firstName) like lower(concat('%', :authorNamePart, '%'))
                           or lower(author.lastName) like lower(concat('%', :authorNamePart, '%'))
                        order by work.title
                        """, Work.class)
                .setParameter("authorNamePart", authorNamePart)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Work> findByIdWithAuthors(Integer workId) {
        return entityManager
                .createQuery("""
                        select distinct work
                        from Work work
                        left join fetch work.authors
                        where work.id = :workId
                        """, Work.class)
                .setParameter("workId", workId)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAuthor(Author author) {
        return entityManager
                .createQuery("""
                        select count(work)
                        from Work work join work.authors author
                        where author = :author
                        """, Long.class)
                .setParameter("author", author)
                .getSingleResult();
    }
}
