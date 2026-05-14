package ru.msu.cmc.webprak.dao;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Cover;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.OrderItem;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.entity.Work;
import ru.msu.cmc.webprak.enums.ItemStatus;
import ru.msu.cmc.webprak.enums.OrderStatus;
import ru.msu.cmc.webprak.enums.UserRole;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Test
    void saveFindUpdateDelete() {
        OrderItem item = persistedOrderItem(ItemStatus.DELIVERED);

        assertThat(orderItemDao.getById(item.getId())).isPresent();

        item.setQuantity(2);
        item.setStatus(ItemStatus.RETURNED);
        orderItemDao.update(item);

        OrderItem updated = orderItemDao.getById(item.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(2);
        assertThat(updated.getStatus()).isEqualTo(ItemStatus.RETURNED);

        orderItemDao.deleteById(item.getId());
        assertThat(orderItemDao.getById(item.getId())).isEmpty();
    }

    @Test
    void findByOrderReturnsCartItems() {
        User client = userDao.save(DaoTestData.user("cartClient", UserRole.CUSTOMER));
        Order cart = orderDao.save(DaoTestData.order(client, OrderStatus.DRAFT));
        Edition first = persistedEdition("Cart work 1");
        Edition second = persistedEdition("Cart work 2");
        orderItemDao.save(DaoTestData.orderItem(cart, first, 1, ItemStatus.DELIVERED));
        orderItemDao.save(DaoTestData.orderItem(cart, second, 2, ItemStatus.DELIVERED));

        assertThat(orderItemDao.findByOrder(cart)).hasSize(2);
        assertThat(orderItemDao.findByOrder(cart, 0, 1)).hasSize(1);
        assertThat(orderItemDao.findByOrderId(cart.getId())).hasSize(2);
    }

    @Test
    void findByStatusReturnsReturnedItemsForRefundFlow() {
        persistedOrderItem(ItemStatus.DELIVERED);
        persistedOrderItem(ItemStatus.RETURNED);

        assertThat(orderItemDao.findByStatus(ItemStatus.RETURNED)).hasSize(1);
    }

    @Test
    void cartQuantityDeleteStatusBatchAndRefundableMethodsSupportItemUseCases() {
        User client = userDao.save(DaoTestData.user("cartOpsClient", UserRole.CUSTOMER));
        Order cart = orderDao.save(DaoTestData.order(client, OrderStatus.DRAFT));
        Edition edition = persistedEdition("Cart ops work");

        OrderItem item = orderItemDao.addToCart(cart, edition, 1);
        orderItemDao.addToCart(cart, edition, 2);

        assertThat(orderItemDao.findByOrderAndEdition(cart, edition)).isPresent();
        assertThat(orderItemDao.existsByOrderAndEdition(cart, edition)).isTrue();
        assertThat(orderItemDao.findByOrderAndStatus(cart, ItemStatus.DELIVERED)).hasSize(1);
        assertThat(orderItemDao.getById(item.getId()).orElseThrow().getQuantity()).isEqualTo(3);
        assertThat(orderItemDao.updateQuantity(item.getId(), 4)).isEqualTo(1);
        assertThat(orderItemDao.findRefundableByOrder(cart)).hasSize(1);
        assertThat(orderItemDao.updateStatus(item.getId(), ItemStatus.RETURNED)).isEqualTo(1);
        assertThat(orderItemDao.updateStatusByIds(java.util.List.of(item.getId()), ItemStatus.DELIVERED)).isEqualTo(1);

        orderItemDao.deleteByOrderAndEdition(cart, edition);
        assertThat(orderItemDao.findByOrderAndEdition(cart, edition)).isEmpty();
    }

    private OrderItem persistedOrderItem(ItemStatus status) {
        User client = userDao.save(DaoTestData.user("itemClient" + status + System.nanoTime(), UserRole.CUSTOMER));
        Order order = orderDao.save(DaoTestData.order(client, OrderStatus.DELIVERED));
        Edition edition = persistedEdition("Item work " + status + System.nanoTime());
        return orderItemDao.save(DaoTestData.orderItem(order, edition, 1, status));
    }

    private Edition persistedEdition(String title) {
        Author author = authorDao.save(DaoTestData.author("Item", "Author" + System.nanoTime()));
        Work work = workDao.save(DaoTestData.work(title, author));
        Cover cover = coverDao.save(DaoTestData.cover("Cover " + title));
        return editionDao.save(DaoTestData.edition(work, cover, 10, new BigDecimal("250.00")));
    }
}
