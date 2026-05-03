package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.OrderStatus;

import java.util.List;

public interface OrderDao extends BaseDao<Order, Integer> {

    List<Order> findByClient(User client);

    List<Order> findByStatus(OrderStatus status);
}
