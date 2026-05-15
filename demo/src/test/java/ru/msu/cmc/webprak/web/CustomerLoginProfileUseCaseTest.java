package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerLoginProfileUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void anonymousCustomerOpeningProfileRedirectsToLogin() {
        driver.get(url("/"));
        driver.findElement(By.cssSelector("a[href*='/profile']")).click();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.findElement(By.id("password")).isDisplayed());
    }

    @Test
    void customerCanLoginAndOpenProfile() {
        loginAsTestUser();

        assertTrue(driver.getCurrentUrl().contains("/profile"));
        assertTrue(driver.getPageSource().contains(testUser.getLogin()));
        assertEquals(testUser.getFirstName(), driver.findElement(By.id("firstName")).getAttribute("value"));
        assertEquals(testUser.getLastName(), driver.findElement(By.id("lastName")).getAttribute("value"));
    }
}
