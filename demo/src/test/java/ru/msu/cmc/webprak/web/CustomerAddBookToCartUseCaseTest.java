package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.msu.cmc.webprak.enums.OrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerAddBookToCartUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void anonymousCustomerAddingBookRedirectsToLogin() {
        driver.get(url("/book/" + testEdition.getId()));
        driver.findElement(By.cssSelector("form[action*='/cart/items'] button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.findElement(By.id("login")).isDisplayed());
    }

    @Test
    void customerCanOpenBookAndAddToCart() {
        loginAsTestUser();

        driver.get(url("/book/" + testEdition.getId()));
        assertTrue(driver.getPageSource().contains(testEdition.getWork().getTitle()));
        driver.findElement(By.name("quantity")).clear();
        driver.findElement(By.name("quantity")).sendKeys("1");
        driver.findElement(By.cssSelector("form[action*='/cart/items'] button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/orders/"));
        Integer cartId = currentOrderId();
        assertEquals(OrderStatus.DRAFT, orderDao.getById(cartId).orElseThrow().getStatus());
        assertTrue(orderItemDao.findByOrderId(cartId).stream()
                .anyMatch(item -> item.getEdition().getId().equals(testEdition.getId())));
    }
}
