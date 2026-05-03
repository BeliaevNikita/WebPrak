package ru.msu.cmc.webprak.dao.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.OrderDao;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.OrderStatus;

import java.util.List;

@Repository
public class OrderDaoImpl extends BaseDaoImpl<Order, Integer> implements OrderDao {

    public OrderDaoImpl() {
        super(Order.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByClient(User client) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("client"), client));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("status"), status));
        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
