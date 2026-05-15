package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import ru.msu.cmc.webprak.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerEditProfileUseCaseTest extends AbstractSeleniumWebTest {

    @Test
    void customerCanEditProfile() {
        loginAsTestUser();

        driver.findElement(By.id("firstName")).clear();
        driver.findElement(By.id("firstName")).sendKeys("Profile");
        driver.findElement(By.id("lastName")).clear();
        driver.findElement(By.id("lastName")).sendKeys("Only");
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys("profile-only-" + testUser.getId() + "@example.com");
        driver.findElement(By.id("phone")).clear();
        driver.findElement(By.id("phone")).sendKeys("+79991112233");
        driver.findElement(By.cssSelector("form[action*='/profile/update'] button[type='submit']")).click();

        waitForInputValue(By.id("firstName"), "Profile");
        clearPersistenceContext();
        User updatedUser = userDao.getById(testUser.getId()).orElseThrow();
        assertEquals("Profile", updatedUser.getFirstName());
        assertEquals("Only", updatedUser.getLastName());
        assertTrue(updatedUser.getContacts().contains("+79991112233"));
    }
}
