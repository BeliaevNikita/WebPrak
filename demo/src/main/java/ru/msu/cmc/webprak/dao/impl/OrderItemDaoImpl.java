package ru.msu.cmc.webprak.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.webprak.dao.OrderItemDao;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.OrderItem;
import ru.msu.cmc.webprak.enums.ItemStatus;

import java.util.List;
import java.util.Optional;

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
    public List<OrderItem> findByOrder(Order order, int offset, int limit) {
        return entityManager
                .createQuery("""
                        select item
                        from OrderItem item
                        left join fetch item.edition edition
                        left join fetch edition.work work
                        left join fetch work.authors
                        left join fetch edition.cover
                        where item.order = :order
                        order by item.id
                        """, OrderItem.class)
                .setParameter("order", order)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findByOrderId(Integer orderId) {
        return entityManager
                .createQuery("""
                        select item
                        from OrderItem item
                        left join fetch item.edition edition
                        left join fetch edition.work work
                        left join fetch work.authors
                        left join fetch edition.cover
                        where item.order.id = :orderId
                        order by item.id
                        """, OrderItem.class)
                .setParameter("orderId", orderId)
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

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findByOrderAndStatus(Order order, ItemStatus status) {
        return entityManager
                .createQuery("""
                        select item
                        from OrderItem item
                        where item.order = :order and item.status = :status
                        order by item.id
                        """, OrderItem.class)
                .setParameter("order", order)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderItem> findByOrderAndEdition(Order order, Edition edition) {
        return entityManager
                .createQuery("""
                        select item
                        from OrderItem item
                        where item.order = :order and item.edition = :edition
                        """, OrderItem.class)
                .setParameter("order", order)
                .setParameter("edition", edition)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrderAndEdition(Order order, Edition edition) {
        return entityManager
                .createQuery("""
                        select count(item)
                        from OrderItem item
                        where item.order = :order and item.edition = :edition
                        """, Long.class)
                .setParameter("order", order)
                .setParameter("edition", edition)
                .getSingleResult() > 0;
    }

    @Override
    @Transactional
    public OrderItem addToCart(Order cart, Edition edition, int quantity) {
        Optional<OrderItem> existingItem = findByOrderAndEdition(cart, edition);
        if (existingItem.isPresent()) {
            OrderItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return update(item);
        }

        OrderItem item = new OrderItem();
        item.setOrder(cart);
        item.setEdition(edition);
        item.setQuantity(quantity);
        item.setStatus(ItemStatus.DELIVERED);
        return save(item);
    }

    @Override
    @Transactional
    public int updateQuantity(Integer itemId, int quantity) {
        return entityManager
                .createQuery("""
                        update OrderItem item
                        set item.quantity = :quantity
                        where item.id = :itemId
                        """)
                .setParameter("itemId", itemId)
                .setParameter("quantity", quantity)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void deleteByOrderAndEdition(Order order, Edition edition) {
        entityManager
                .createQuery("""
                        delete from OrderItem item
                        where item.order = :order and item.edition = :edition
                        """)
                .setParameter("order", order)
                .setParameter("edition", edition)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int updateStatus(Integer itemId, ItemStatus status) {
        return entityManager
                .createQuery("""
                        update OrderItem item
                        set item.status = :status
                        where item.id = :itemId
                        """)
                .setParameter("itemId", itemId)
                .setParameter("status", status)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int updateStatusByIds(List<Integer> itemIds, ItemStatus status) {
        if (itemIds == null || itemIds.isEmpty()) {
            return 0;
        }
        return entityManager
                .createQuery("""
                        update OrderItem item
                        set item.status = :status
                        where item.id in :itemIds
                        """)
                .setParameter("itemIds", itemIds)
                .setParameter("status", status)
                .executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findRefundableByOrder(Order order) {
        return entityManager
                .createQuery("""
                        select item
                        from OrderItem item
                        left join fetch item.edition edition
                        left join fetch edition.work work
                        left join fetch work.authors
                        left join fetch edition.cover
                        where item.order = :order and item.status = :status
                        order by item.id
                        """, OrderItem.class)
                .setParameter("order", order)
                .setParameter("status", ItemStatus.DELIVERED)
                .getResultList();
    }
}
