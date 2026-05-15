package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeLoginUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void employeeCanLoginAndSeeDashboard() {
        loginAsEmployee();

        assertTrue(driver.getCurrentUrl().contains("/admin"));
        assertTrue(driver.getPageSource().contains("Панель управления"));
        assertFalse(driver.findElements(By.cssSelector("a[href='/admin/books']")).isEmpty());
        assertFalse(driver.findElements(By.cssSelector("a[href='/admin/orders']")).isEmpty());
    }
}
