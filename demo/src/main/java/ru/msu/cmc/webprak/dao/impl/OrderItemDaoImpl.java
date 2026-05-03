package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.OrderItemDao;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.OrderItem;
import ru.msu.cmc.webprak.enums.ItemStatus;

import java.util.List;

@Repository
public class OrderItemDaoImpl extends BaseDaoImpl<OrderItem, Integer> implements OrderItemDao {

    public OrderItemDaoImpl() {
        super(OrderItem.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findByOrder(Order order) {
        return entityManager
                .createQuery("select item from OrderItem item where item.order = :order", OrderItem.class)
                .setParameter("order", order)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findByStatus(ItemStatus status) {
        return entityManager
                .createQuery("select item from OrderItem item where item.status = :status", OrderItem.class)
                .setParameter("status", status)
                .getResultList();
    }
}
