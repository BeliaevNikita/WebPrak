package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import ru.msu.cmc.webprak.enums.ItemStatus;
import ru.msu.cmc.webprak.enums.OrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeRefundApprovalUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void employeeCanApproveRefundForSelectedItems() {
        orderDao.updateStatus(employeeManagedOrder.getId(), OrderStatus.REFUND_ATTEMPT);
        loginAsEmployee();

        driver.get(url("/orders/" + employeeManagedOrder.getId()));
        WebElement itemCheckbox = driver.findElement(By.name("itemIds"));
        assertTrue(itemCheckbox.isDisplayed());
        itemCheckbox.click();
        driver.findElement(By.cssSelector("form[action*='/admin/orders/'] button[type='submit']")).click();

        wait.until(webDriver -> orderDao.getById(employeeManagedOrder.getId())
                .map(order -> order.getStatus() == OrderStatus.REFUND_ACCEPTED)
                .orElse(false));
        assertEquals(ItemStatus.RETURNED, orderItemDao.findByOrderId(employeeManagedOrder.getId()).get(0).getStatus());
    }
}
