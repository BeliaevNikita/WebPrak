package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.enums.OrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeOrderStatusUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void employeeCanChangeOrderStatus() {
        loginAsEmployee();
        driver.get(url("/admin/orders?client=" + testUser.getLogin()));
        wait.until(webDriver -> webDriver.getPageSource().contains(testUser.getLogin()));

        changeOrderStatusFromAdminTable(employeeManagedOrder.getId(), OrderStatus.DELIVERED);

        wait.until(webDriver -> orderDao.getById(employeeManagedOrder.getId())
                .map(order -> order.getStatus() == OrderStatus.DELIVERED)
                .orElse(false));
        assertEquals(OrderStatus.DELIVERED, orderDao.getById(employeeManagedOrder.getId()).orElseThrow().getStatus());
    }
}
