package ru.msu.cmc.webprak.dao.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.UserDao;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl extends BaseDaoImpl<User, Integer> implements UserDao {

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("login"), login));
        return entityManager.createQuery(criteriaQuery).getResultStream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return entityManager
                .createNativeQuery("select * from users where contacts ->> 'email' = :email", User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultStream()
                .map(User.class::cast)
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByLoginAndPassword(String login, String password) {
        return entityManager
                .createQuery("""
                        select user
                        from User user
                        where user.login = :login and user.password = :password
                        """, User.class)
                .setParameter("login", login)
                .setParameter("password", password)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(UserRole role) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("role"), role));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchByName(String namePart, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select user
                        from User user
                        where lower(user.firstName) like lower(concat('%', :namePart, '%'))
                           or lower(user.lastName) like lower(concat('%', :namePart, '%'))
                           or lower(user.login) like lower(concat('%', :namePart, '%'))
                        order by user.lastName, user.firstName, user.login
                        """, User.class)
                .setParameter("namePart", namePart)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLogin(String login) {
        return entityManager
                .createQuery("select count(user) from User user where user.login = :login", Long.class)
                .setParameter("login", login)
                .getSingleResult() > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        Number count = (Number) entityManager
                .createNativeQuery("select count(*) from users where contacts ->> 'email' = :email")
                .setParameter("email", email)
                .getSingleResult();
        return count.longValue() > 0;
    }
}
