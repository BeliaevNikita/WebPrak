package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.AuthorDao;
import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;

@Repository
public class AuthorDaoImpl extends BaseDaoImpl<Author, Integer> implements AuthorDao {

    public AuthorDaoImpl() {
        super(Author.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findByLastName(String lastName) {
        return entityManager
                .createQuery("select author from Author author where author.lastName = :lastName", Author.class)
                .setParameter("lastName", lastName)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findByName(String firstName, String lastName) {
        return entityManager
                .createQuery("""
                        select author
                        from Author author
                        where author.firstName = :firstName and author.lastName = :lastName
                        """, Author.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> searchByNamePartIgnoreCase(String namePart, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select author
                        from Author author
                        where lower(author.firstName) like lower(concat('%', :namePart, '%'))
                           or lower(author.lastName) like lower(concat('%', :namePart, '%'))
                        order by author.lastName, author.firstName
                        """, Author.class)
                .setParameter("namePart", namePart)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findByWork(Work work) {
        return entityManager
                .createQuery("""
                        select author
                        from Work work join work.authors author
                        where work = :work
                        order by author.lastName, author.firstName
                        """, Author.class)
                .setParameter("work", work)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String firstName, String lastName) {
        return entityManager
                .createQuery("""
                        select count(author)
                        from Author author
                        where author.firstName = :firstName and author.lastName = :lastName
                        """, Long.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .getSingleResult() > 0;
    }
}
