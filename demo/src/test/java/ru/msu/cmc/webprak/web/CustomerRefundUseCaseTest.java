package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.msu.cmc.webprak.enums.OrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerRefundUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void customerCanOpenOrderDetails() {
        loginAsTestUser();

        driver.findElement(By.cssSelector("a[href='/orders/" + deliveredOrder.getId() + "']")).click();

        wait.until(ExpectedConditions.urlContains("/orders/" + deliveredOrder.getId()));
        assertTrue(driver.getPageSource().contains(testEdition.getWork().getTitle()));
        assertTrue(driver.getPageSource().contains("DELIVERED"));
    }

    @Test
    void customerCanRequestRefundForSelectedItems() {
        loginAsTestUser();

        driver.get(url("/orders/" + deliveredOrder.getId()));
        WebElement itemCheckbox = driver.findElement(By.name("itemIds"));
        assertTrue(itemCheckbox.isDisplayed());
        itemCheckbox.click();
        driver.findElement(By.cssSelector("form[action*='/refund'] button[type='submit']")).click();

        wait.until(webDriver -> orderDao.getById(deliveredOrder.getId())
                .map(order -> order.getStatus() == OrderStatus.REFUND_ATTEMPT)
                .orElse(false));
        assertEquals(OrderStatus.REFUND_ATTEMPT, orderDao.getById(deliveredOrder.getId()).orElseThrow().getStatus());
    }
}
