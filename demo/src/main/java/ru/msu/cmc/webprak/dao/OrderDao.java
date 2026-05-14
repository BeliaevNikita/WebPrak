package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderDao extends BaseDao<Order, Integer> {

    List<Order> findByClient(User client);

    List<Order> findByClient(User client, int offset, int limit);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByStatus(OrderStatus status, int offset, int limit);

    Optional<Order> findActiveCartByClient(User client);

    Optional<Order> findByIdWithItems(Integer orderId);

    List<Order> findByClientAndStatus(User client, OrderStatus status, int offset, int limit);

    List<Order> findByOrderDateBetween(LocalDate from, LocalDate to, int offset, int limit);

    List<Order> searchOrders(OrderStatus status, User client, LocalDate from, LocalDate to, int offset, int limit);

    long countByClient(User client);

    long countByStatus(OrderStatus status);

    long countSearchOrders(OrderStatus status, User client, LocalDate from, LocalDate to);

    int updateStatus(Integer orderId, OrderStatus status);

    int checkout(Integer orderId, String deliveryAddress, LocalDate orderDate);

    BigDecimal calculateTotal(Integer orderId);
}
