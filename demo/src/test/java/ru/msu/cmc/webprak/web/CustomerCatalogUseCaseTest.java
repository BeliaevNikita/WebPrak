package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerCatalogUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void customerCanSearchAndFilterCatalog() {
        loginAsTestUser();

        driver.get(url("/catalog"));
        driver.findElement(By.name("title")).sendKeys(testEdition.getWork().getTitle());
        driver.findElement(By.cssSelector("form button[type='submit']")).click();
        waitForAnyElementText(By.cssSelector(".card-title"), testEdition.getWork().getTitle());

        driver.get(url("/catalog?author=" + testEdition.getWork().getAuthors().get(0).getLastName()
                + "&minPrice=700&maxPrice=800"));
        waitForAnyElementText(By.cssSelector(".card-title"), testEdition.getWork().getTitle());
        assertTrue(driver.getPageSource().contains(testEdition.getPublisher()));
    }
}
