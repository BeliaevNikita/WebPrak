package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.msu.cmc.webprak.entity.Edition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeBookManagementUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void employeeCanAddBook() {
        loginAsEmployee();
        driver.findElement(By.cssSelector("a[href='/admin/books']")).click();
        wait.until(ExpectedConditions.urlContains("/admin/books"));

        String title = addBookFromAdminForm("Add Only Publisher", "9");

        assertTrue(driver.getPageSource().contains(title));
        assertFalse(editionDao.searchByTitle(title, 0, 10).isEmpty());
    }

    @Test
    void employeeCanEditBook() {
        loginAsEmployee();
        driver.get(url("/admin/books"));
        wait.until(ExpectedConditions.urlContains("/admin/books"));

        WebElement row = rowContaining(testEdition.getWork().getTitle());
        row.findElement(By.cssSelector("input[name='publisher']")).clear();
        row.findElement(By.cssSelector("input[name='publisher']")).sendKeys("Separate Edit Publisher");
        row.findElement(By.cssSelector("input[name='quantity']")).clear();
        row.findElement(By.cssSelector("input[name='quantity']")).sendKeys("4");
        row.findElement(By.cssSelector("form[action*='/update']")).submit();

        wait.until(webDriver -> webDriver.getPageSource().contains("Separate Edit Publisher"));
        clearPersistenceContext();
        Edition updatedEdition = editionDao.getById(testEdition.getId()).orElseThrow();
        assertEquals("Separate Edit Publisher", updatedEdition.getPublisher());
        assertEquals(4, updatedEdition.getQuantity());
    }

    @Test
    void employeeCanDeleteBook() {
        loginAsEmployee();
        driver.get(url("/admin/books"));
        wait.until(ExpectedConditions.urlContains("/admin/books"));
        String title = addBookFromAdminForm("Delete Only Publisher", "2");

        rowContaining(title).findElement(By.cssSelector("form[action*='/delete']")).submit();

        wait.until(webDriver -> webDriver.findElements(By.xpath("//tbody/tr[contains(., '" + title + "')]")).isEmpty());
        assertTrue(editionDao.searchByTitle(title, 0, 10).isEmpty());
    }
}
