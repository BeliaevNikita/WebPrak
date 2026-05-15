package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.ItemStatus;
import ru.msu.cmc.webprak.enums.OrderStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeleniumWebIntegrationTest extends AbstractSeleniumWebTest {

    @Test
    void anonymousCustomerCanNavigateButAddingBookRequiresLogin() {
        driver.get(url("/"));

        driver.findElement(By.cssSelector("a[href*='/catalog']")).click();
        wait.until(ExpectedConditions.urlContains("/catalog"));
        assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Каталог"));

        driver.findElement(By.cssSelector("a[href*='/profile']")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.findElement(By.id("login")).isDisplayed());

        driver.get(url("/book/" + testEdition.getId()));
        driver.findElement(By.cssSelector("form[action*='/cart/items'] button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.findElement(By.id("password")).isDisplayed());
    }

    @Test
    void customerCanSearchFilterOpenBookAddCartFromProfileAndCheckoutSelectedItems() {
        loginAsTestUser();

        driver.get(url("/catalog"));
        driver.findElement(By.name("title")).sendKeys(testEdition.getWork().getTitle());
        driver.findElement(By.cssSelector("form button[type='submit']")).click();
        waitForAnyElementText(By.cssSelector(".card-title"), testEdition.getWork().getTitle());

        driver.get(url("/catalog?author=" + testEdition.getWork().getAuthors().get(0).getLastName()
                + "&minPrice=700&maxPrice=800"));
        waitForAnyElementText(By.cssSelector(".card-title"), testEdition.getWork().getTitle());
        assertTrue(driver.getPageSource().contains(testEdition.getPublisher()));

        driver.findElement(By.cssSelector("a[href='/book/" + testEdition.getId() + "']")).click();
        wait.until(ExpectedConditions.urlContains("/book/" + testEdition.getId()));
        driver.findElement(By.name("quantity")).clear();
        driver.findElement(By.name("quantity")).sendKeys("1");
        driver.findElement(By.cssSelector("form[action*='/cart/items'] button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/orders/"));
        Integer cartId = currentOrderId();

        driver.get(url("/profile"));
        wait.until(ExpectedConditions.urlContains("/profile"));
        assertTrue(driver.getPageSource().contains("DRAFT"));
        driver.findElement(By.cssSelector("a[href='/orders/" + cartId + "']")).click();
        wait.until(ExpectedConditions.urlContains("/orders/" + cartId));

        WebElement itemCheckbox = driver.findElement(By.name("itemIds"));
        assertTrue(itemCheckbox.isDisplayed());
        itemCheckbox.click();
        driver.findElement(By.name("deliveryAddress")).sendKeys("Selenium delivery address");
        driver.findElement(By.cssSelector("form[action*='/checkout'] button[type='submit']")).click();

        wait.until(webDriver -> orderDao.getById(cartId)
                .map(order -> order.getStatus() == OrderStatus.IN_PROCESSING)
                .orElse(false));
        assertTrue(orderDao.findActiveCartByClient(testUser).isEmpty());
        assertEquals("Selenium delivery address", orderDao.getById(cartId).orElseThrow().getDeliveryAddress());
    }

    @Test
    void customerCanEditProfileOpenOrderDetailsAndRequestRefundForSelectedItems() {
        loginAsTestUser();

        driver.findElement(By.id("firstName")).clear();
        driver.findElement(By.id("firstName")).sendKeys("Updated");
        driver.findElement(By.id("lastName")).clear();
        driver.findElement(By.id("lastName")).sendKeys("Customer");
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys("updated-" + testUser.getId() + "@example.com");
        driver.findElement(By.id("phone")).clear();
        driver.findElement(By.id("phone")).sendKeys("+79990000000");
        driver.findElement(By.cssSelector("form[action*='/profile/update'] button[type='submit']")).click();
        waitForInputValue(By.id("firstName"), "Updated");

        clearPersistenceContext();
        User updatedUser = userDao.getById(testUser.getId()).orElseThrow();
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Customer", updatedUser.getLastName());
        assertTrue(updatedUser.getContacts().contains("+79990000000"));

        driver.findElement(By.cssSelector("a[href='/orders/" + deliveredOrder.getId() + "']")).click();
        wait.until(ExpectedConditions.urlContains("/orders/" + deliveredOrder.getId()));
        WebElement itemCheckbox = driver.findElement(By.name("itemIds"));
        assertTrue(itemCheckbox.isDisplayed());
        itemCheckbox.click();
        driver.findElement(By.cssSelector("form[action*='/refund'] button[type='submit']")).click();

        wait.until(webDriver -> orderDao.getById(deliveredOrder.getId())
                .map(order -> order.getStatus() == OrderStatus.REFUND_ATTEMPT)
                .orElse(false));
        assertEquals(OrderStatus.REFUND_ATTEMPT, orderDao.getById(deliveredOrder.getId()).orElseThrow().getStatus());
    }

    @Test
    void employeeLoginShowsDashboard() {
        loginAsEmployee();

        assertTrue(driver.getCurrentUrl().contains("/admin"));
        assertTrue(driver.getPageSource().contains("Панель управления"));
        assertFalse(driver.findElements(By.cssSelector("a[href='/admin/books']")).isEmpty());
        assertFalse(driver.findElements(By.cssSelector("a[href='/admin/orders']")).isEmpty());
    }

    @Test
    void employeeCanAddEditAndDeleteBookFromAdminBooksPage() {
        loginAsEmployee();
        driver.findElement(By.cssSelector("a[href='/admin/books']")).click();
        wait.until(ExpectedConditions.urlContains("/admin/books"));

        String title = addBookFromAdminForm("Selenium Admin Publisher", "8");

        WebElement row = rowContaining(title);
        row.findElement(By.cssSelector("input[name='publisher']")).clear();
        row.findElement(By.cssSelector("input[name='publisher']")).sendKeys("Updated Publisher");
        row.findElement(By.cssSelector("input[name='quantity']")).clear();
        row.findElement(By.cssSelector("input[name='quantity']")).sendKeys("3");
        row.findElement(By.cssSelector("form[action*='/update']")).submit();
        wait.until(webDriver -> webDriver.getPageSource().contains("Updated Publisher"));

        row = rowContaining(title);
        row.findElement(By.cssSelector("form[action*='/delete']")).submit();
        wait.until(webDriver -> webDriver.findElements(By.xpath("//tbody/tr[contains(., '" + title + "')]")).isEmpty());
        assertTrue(editionDao.searchByTitle(title, 0, 10).isEmpty());
    }

    @Test
    void employeeCanChangeOrderStatusAndApproveSelectedRefundItems() {
        loginAsEmployee();
        driver.findElement(By.cssSelector("a[href='/admin/orders']")).click();
        wait.until(ExpectedConditions.urlContains("/admin/orders"));

        driver.findElement(By.id("client")).sendKeys(testUser.getLogin());
        driver.findElement(By.cssSelector("form[action*='/admin/orders'] button[type='submit']")).click();
        wait.until(webDriver -> webDriver.getPageSource().contains(testUser.getLogin()));

        changeOrderStatusFromAdminTable(employeeManagedOrder.getId(), OrderStatus.DELIVERED);
        wait.until(webDriver -> orderDao.getById(employeeManagedOrder.getId())
                .map(order -> order.getStatus() == OrderStatus.DELIVERED)
                .orElse(false));

        orderDao.updateStatus(employeeManagedOrder.getId(), OrderStatus.REFUND_ATTEMPT);
        driver.get(url("/orders/" + employeeManagedOrder.getId()));
        wait.until(ExpectedConditions.urlContains("/orders/" + employeeManagedOrder.getId()));

        WebElement itemCheckbox = driver.findElement(By.name("itemIds"));
        assertTrue(itemCheckbox.isDisplayed());
        itemCheckbox.click();
        driver.findElement(By.cssSelector("form[action*='/admin/orders/'] button[type='submit']")).click();

        wait.until(webDriver -> orderDao.getById(employeeManagedOrder.getId())
                .map(order -> order.getStatus() == OrderStatus.REFUND_ACCEPTED)
                .orElse(false));
        assertEquals(OrderStatus.REFUND_ACCEPTED, orderDao.getById(employeeManagedOrder.getId()).orElseThrow().getStatus());
        assertEquals(ItemStatus.RETURNED, orderItemDao.findByOrderId(employeeManagedOrder.getId()).get(0).getStatus());
    }
}
