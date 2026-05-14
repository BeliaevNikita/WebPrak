package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.OrderDao;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderDaoImpl extends BaseDaoImpl<Order, Integer> implements OrderDao {

    public OrderDaoImpl() {
        super(Order.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByClient(User client) {
        return entityManager
                .createQuery("""
                        select order
                        from Order order
                        where order.client = :client
                        order by order.orderDate desc, order.id desc
                        """, Order.class)
                .setParameter("client", client)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByClient(User client, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select order
                        from Order order
                        where order.client = :client
                        order by order.orderDate desc, order.id desc
                        """, Order.class)
                .setParameter("client", client)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        return entityManager
                .createQuery("""
                        select order
                        from Order order
                        where order.status = :status
                        order by order.orderDate desc, order.id desc
                        """, Order.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select order
                        from Order order
                        where order.status = :status
                        order by order.orderDate desc, order.id desc
                        """, Order.class)
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findActiveCartByClient(User client) {
        return entityManager
                .createQuery("""
                        select order
                        from Order order
                        where order.client = :client and order.status = :status
                        order by order.id desc
                        """, Order.class)
                .setParameter("client", client)
                .setParameter("status", OrderStatus.DRAFT)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByIdWithItems(Integer orderId) {
        return entityManager
                .createQuery("""
                        select distinct order
                        from Order order
                        left join fetch order.items item
                        left join fetch item.edition edition
                        left join fetch edition.work work
                        left join fetch edition.cover
                        where order.id = :orderId
                        """, Order.class)
                .setParameter("orderId", orderId)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByClientAndStatus(User client, OrderStatus status, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select order
                        from Order order
                        where order.client = :client and order.status = :status
                        order by order.orderDate desc, order.id desc
                        """, Order.class)
                .setParameter("client", client)
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByOrderDateBetween(LocalDate from, LocalDate to, int offset, int limit) {
        return searchOrders(null, null, from, to, offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> searchOrders(OrderStatus status, User client, LocalDate from, LocalDate to, int offset, int limit) {
        var query = entityManager.createQuery(buildSearchOrdersQuery(false, status, client, from, to), Order.class);
        setSearchOrdersParameters(query, status, client, from, to);
        return query.setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByClient(User client) {
        return entityManager
                .createQuery("select count(order) from Order order where order.client = :client", Long.class)
                .setParameter("client", client)
                .getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        return entityManager
                .createQuery("select count(order) from Order order where order.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public long countSearchOrders(OrderStatus status, User client, LocalDate from, LocalDate to) {
        var query = entityManager.createQuery(buildSearchOrdersQuery(true, status, client, from, to), Long.class);
        setSearchOrdersParameters(query, status, client, from, to);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public int updateStatus(Integer orderId, OrderStatus status) {
        return entityManager
                .createQuery("""
                        update Order order
                        set order.status = :status
                        where order.id = :orderId
                        """)
                .setParameter("orderId", orderId)
                .setParameter("status", status)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int checkout(Integer orderId, String deliveryAddress, LocalDate orderDate) {
        return entityManager
                .createQuery("""
                        update Order order
                        set order.status = :status,
                            order.deliveryAddress = :deliveryAddress,
                            order.orderDate = :orderDate
                        where order.id = :orderId and order.status = :draftStatus
                        """)
                .setParameter("orderId", orderId)
                .setParameter("status", OrderStatus.IN_PROCESSING)
                .setParameter("draftStatus", OrderStatus.DRAFT)
                .setParameter("deliveryAddress", deliveryAddress)
                .setParameter("orderDate", orderDate)
                .executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotal(Integer orderId) {
        BigDecimal total = entityManager
                .createQuery("""
                        select sum(item.quantity * edition.price)
                        from OrderItem item join item.edition edition
                        where item.order.id = :orderId
                        """, BigDecimal.class)
                .setParameter("orderId", orderId)
                .getSingleResult();
        return total == null ? BigDecimal.ZERO : total;
    }

    private String buildSearchOrdersQuery(boolean count, OrderStatus status, User client, LocalDate from, LocalDate to) {
        StringBuilder jpql = new StringBuilder(count
                ? "select count(order) from Order order where 1 = 1"
                : "select order from Order order where 1 = 1");
        if (status != null) {
            jpql.append(" and order.status = :status");
        }
        if (client != null) {
            jpql.append(" and order.client = :client");
        }
        if (from != null) {
            jpql.append(" and order.orderDate >= :from");
        }
        if (to != null) {
            jpql.append(" and order.orderDate <= :to");
        }
        if (!count) {
            jpql.append(" order by order.orderDate desc, order.id desc");
        }
        return jpql.toString();
    }

    private void setSearchOrdersParameters(jakarta.persistence.Query query,
                                           OrderStatus status,
                                           User client,
                                           LocalDate from,
                                           LocalDate to) {
        if (status != null) {
            query.setParameter("status", status);
        }
        if (client != null) {
            query.setParameter("client", client);
        }
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
    }
}
