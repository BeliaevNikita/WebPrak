package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.enums.OrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerCheckoutUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void customerCanOpenDraftCartFromProfile() {
        Order cart = createCartWithItem();
        loginAsTestUser();

        assertTrue(driver.getPageSource().contains("DRAFT"));
        driver.findElement(By.cssSelector("a[href='/orders/" + cart.getId() + "']")).click();

        wait.until(ExpectedConditions.urlContains("/orders/" + cart.getId()));
        assertTrue(driver.getPageSource().contains(testEdition.getWork().getTitle()));
    }

    @Test
    void customerCanCheckoutSelectedCartItems() {
        Order cart = createCartWithItem();
        loginAsTestUser();

        driver.get(url("/orders/" + cart.getId()));
        WebElement itemCheckbox = driver.findElement(By.name("itemIds"));
        assertTrue(itemCheckbox.isDisplayed());
        itemCheckbox.click();
        driver.findElement(By.name("deliveryAddress")).sendKeys("Separate checkout address");
        driver.findElement(By.cssSelector("form[action*='/checkout'] button[type='submit']")).click();

        wait.until(webDriver -> orderDao.getById(cart.getId())
                .map(order -> order.getStatus() == OrderStatus.IN_PROCESSING)
                .orElse(false));
        assertEquals("Separate checkout address", orderDao.getById(cart.getId()).orElseThrow().getDeliveryAddress());
    }
}
