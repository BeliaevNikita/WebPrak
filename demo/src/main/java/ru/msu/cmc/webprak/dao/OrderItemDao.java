package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.OrderItem;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.enums.ItemStatus;

import java.util.List;
import java.util.Optional;

public interface OrderItemDao extends BaseDao<OrderItem, Integer> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByOrder(Order order, int offset, int limit);

    List<OrderItem> findByOrderId(Integer orderId);

    List<OrderItem> findByStatus(ItemStatus status);

    List<OrderItem> findByOrderAndStatus(Order order, ItemStatus status);

    Optional<OrderItem> findByOrderAndEdition(Order order, Edition edition);

    boolean existsByOrderAndEdition(Order order, Edition edition);

    OrderItem addToCart(Order cart, Edition edition, int quantity);

    int updateQuantity(Integer itemId, int quantity);

    void deleteByOrderAndEdition(Order order, Edition edition);

    int updateStatus(Integer itemId, ItemStatus status);

    int updateStatusByIds(List<Integer> itemIds, ItemStatus status);

    List<OrderItem> findRefundableByOrder(Order order);
}
