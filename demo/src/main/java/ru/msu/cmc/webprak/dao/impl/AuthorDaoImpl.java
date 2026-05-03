package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.AuthorDao;
import ru.msu.cmc.webprak.entity.Author;

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
}
