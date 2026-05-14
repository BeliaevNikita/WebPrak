package ru.msu.cmc.webprak.dao;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.OrderStatus;
import ru.msu.cmc.webprak.enums.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Test
    void saveFindUpdateDelete() {
        User client = userDao.save(DaoTestData.user("orderClient", UserRole.CUSTOMER));
        Order order = orderDao.save(DaoTestData.order(client, OrderStatus.DRAFT));

        assertThat(orderDao.getById(order.getId())).isPresent();

        order.setStatus(OrderStatus.IN_PROCESSING);
        order.setDeliveryAddress("Updated address");
        orderDao.update(order);

        Order updated = orderDao.getById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.IN_PROCESSING);
        assertThat(updated.getDeliveryAddress()).isEqualTo("Updated address");

        orderDao.deleteById(order.getId());
        assertThat(orderDao.getById(order.getId())).isEmpty();
    }

    @Test
    void findByClientReturnsCartAndHistoricalOrders() {
        User client = userDao.save(DaoTestData.user("clientWithOrders", UserRole.CUSTOMER));
        orderDao.save(DaoTestData.order(client, OrderStatus.DRAFT));
        orderDao.save(DaoTestData.order(client, OrderStatus.DELIVERED));

        assertThat(orderDao.findByClient(client))
                .extracting(Order::getStatus)
                .containsExactlyInAnyOrder(OrderStatus.DRAFT, OrderStatus.DELIVERED);
        assertThat(orderDao.findByClient(client, 0, 1)).hasSize(1);
        assertThat(orderDao.countByClient(client)).isEqualTo(2);
        assertThat(orderDao.findActiveCartByClient(client)).isPresent();
    }

    @Test
    void findByStatusSupportsCartAndBackofficeFilters() {
        User first = userDao.save(DaoTestData.user("draftClient", UserRole.CUSTOMER));
        User second = userDao.save(DaoTestData.user("refundClient", UserRole.CUSTOMER));
        orderDao.save(DaoTestData.order(first, OrderStatus.DRAFT));
        orderDao.save(DaoTestData.order(second, OrderStatus.REFUND_ATTEMPT));

        assertThat(orderDao.findByStatus(OrderStatus.DRAFT)).hasSize(1);
        assertThat(orderDao.findByStatus(OrderStatus.DRAFT, 0, 10)).hasSize(1);
        assertThat(orderDao.findByStatus(OrderStatus.REFUND_ATTEMPT)).hasSize(1);
        assertThat(orderDao.countByStatus(OrderStatus.REFUND_ATTEMPT)).isEqualTo(1);
    }

    @Test
    void findByClientReturnsEmptyListForClientWithoutOrders() {
        User client = userDao.save(DaoTestData.user("emptyClient", UserRole.CUSTOMER));

        assertThat(orderDao.findByClient(client)).isEmpty();
    }

    @Test
    void checkoutStatusTotalAndBackofficeSearchMethodsSupportOrderUseCases() {
        User client = userDao.save(DaoTestData.user("checkoutClient", UserRole.CUSTOMER));
        Order cart = orderDao.save(DaoTestData.order(client, OrderStatus.DRAFT));
        var author = authorDao.save(DaoTestData.author("Checkout", "Author"));
        var work = workDao.save(DaoTestData.work("Checkout Work", author));
        var cover = coverDao.save(DaoTestData.cover("Checkout Cover"));
        var edition = editionDao.save(DaoTestData.edition(work, cover, 5, new java.math.BigDecimal("120.00")));
        orderItemDao.save(DaoTestData.orderItem(cart, edition, 2, ru.msu.cmc.webprak.enums.ItemStatus.DELIVERED));

        assertThat(orderDao.findByIdWithItems(cart.getId())).isPresent();
        assertThat(orderDao.findByClientAndStatus(client, OrderStatus.DRAFT, 0, 10)).contains(cart);
        assertThat(orderDao.calculateTotal(cart.getId())).isEqualByComparingTo("240.00");
        assertThat(orderDao.checkout(cart.getId(), "New address", java.time.LocalDate.of(2024, 3, 1))).isEqualTo(1);
        assertThat(orderDao.searchOrders(OrderStatus.IN_PROCESSING, client, java.time.LocalDate.of(2024, 1, 1),
                java.time.LocalDate.of(2024, 12, 31), 0, 10)).hasSize(1);
        assertThat(orderDao.countSearchOrders(OrderStatus.IN_PROCESSING, client, null, null)).isEqualTo(1);
        assertThat(orderDao.updateStatus(cart.getId(), OrderStatus.DELIVERED)).isEqualTo(1);
        assertThat(orderDao.findByOrderDateBetween(java.time.LocalDate.of(2024, 1, 1),
                java.time.LocalDate.of(2024, 12, 31), 0, 10)).isNotEmpty();
    }
}
