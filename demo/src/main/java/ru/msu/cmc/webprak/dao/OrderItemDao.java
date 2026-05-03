package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.OrderItem;
import ru.msu.cmc.webprak.enums.ItemStatus;

import java.util.List;

public interface OrderItemDao extends BaseDao<OrderItem, Integer> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByStatus(ItemStatus status);
}
