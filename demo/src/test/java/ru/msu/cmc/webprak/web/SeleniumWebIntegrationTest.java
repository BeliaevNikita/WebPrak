package ru.msu.cmc.webprak.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.msu.cmc.webprak.WebPrakApplication;
import ru.msu.cmc.webprak.dao.AuthorDao;
import ru.msu.cmc.webprak.dao.CoverDao;
import ru.msu.cmc.webprak.dao.EditionDao;
import ru.msu.cmc.webprak.dao.OrderDao;
import ru.msu.cmc.webprak.dao.OrderItemDao;
import ru.msu.cmc.webprak.dao.UserDao;
import ru.msu.cmc.webprak.dao.WorkDao;
import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Cover;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.OrderItem;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.entity.Work;
import ru.msu.cmc.webprak.enums.ItemStatus;
import ru.msu.cmc.webprak.enums.OrderStatus;
import ru.msu.cmc.webprak.enums.UserRole;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = WebPrakApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SeleniumWebIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("book_store_web_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }

    @LocalServerPort
    private int port;

    @Autowired
    private AuthorDao authorDao;

    @Autowired
    private CoverDao coverDao;

    @Autowired
    private EditionDao editionDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private WorkDao workDao;

    private WebDriver driver;
    private WebDriverWait wait;
    private Edition testEdition;
    private User testUser;
    private User employeeUser;
    private Order employeeManagedOrder;

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @BeforeEach
    void setUp() {
        seedData();
        driver = createChromeDriverOrSkip();
        driver.manage().window().setPosition(new Point(0, 0));
        driver.manage().window().setSize(new Dimension(1440, 1000));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void navigationBetweenMainPagesWorks() {
        driver.get(url("/"));
        assertEquals("Главная", driver.getTitle());

        driver.findElement(By.cssSelector("a[href*='/catalog']")).click();
        wait.until(ExpectedConditions.urlContains("/catalog"));
        assertEquals("Каталог", driver.getTitle());
        assertNotNull(driver.findElement(By.tagName("h1")));
        assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Каталог"));

        driver.findElement(By.cssSelector("a[href*='/profile']")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        assertEquals("Вход", driver.getTitle());
        assertNotNull(driver.findElement(By.id("login")));

        driver.findElement(By.cssSelector("a[href^='/']")).click();
        wait.until(ExpectedConditions.titleIs("Главная"));
        assertTrue(driver.findElements(By.cssSelector("a[href*='/catalog']")).size() > 0);
    }

    @Test
    void bookSearchShowsMatchingBook() {
        driver.get(url("/catalog"));

        WebElement search = driver.findElement(By.name("title"));
        search.clear();
        search.sendKeys(testEdition.getWork().getTitle());
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".card-title"), testEdition.getWork().getTitle()));
        assertEquals("Каталог", driver.getTitle());
        assertTrue(driver.getPageSource().contains(testEdition.getPublisher()));
        assertTrue(driver.findElements(By.cssSelector(".card-title")).stream()
                .anyMatch(card -> card.getText().contains(testEdition.getWork().getTitle())));
    }

    @Test
    void userCanLogin() {
        loginAsTestUser();

        assertTrue(driver.getCurrentUrl().contains("/profile"));
        assertEquals("Личный кабинет", driver.getTitle());
        assertTrue(driver.getPageSource().contains(testUser.getLogin()));
        assertEquals(testUser.getFirstName(), driver.findElement(By.id("firstName")).getAttribute("value"));
        assertEquals(testUser.getLastName(), driver.findElement(By.id("lastName")).getAttribute("value"));
    }

    @Test
    void userCanAddBookToCartAndCheckout() {
        loginAsTestUser();
        driver.get(url("/book/" + testEdition.getId()));

        WebElement quantity = driver.findElement(By.name("quantity"));
        quantity.clear();
        quantity.sendKeys("1");
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/orders/"));
        assertEquals("Заказ", driver.getTitle());
        assertTrue(driver.getPageSource().contains(testEdition.getWork().getTitle()));

        driver.findElement(By.name("deliveryAddress")).sendKeys("Selenium delivery address");
        driver.findElement(By.cssSelector("form[action*='/checkout'] button[type='submit']")).click();
        wait.until(webDriver -> webDriver.getPageSource().contains("IN_PROCESSING"));

        assertTrue(orderDao.findActiveCartByClient(testUser).isEmpty());
        assertTrue(orderDao.findByClient(testUser).stream()
                .anyMatch(order -> "Selenium delivery address".equals(order.getDeliveryAddress())));
    }

    @Test
    void employeeLoginShowsDashboardAndAdminNavigationLink() {
        loginAsEmployee();

        assertTrue(driver.getCurrentUrl().contains("/admin"));
        assertEquals("Панель управления", driver.getTitle());
        assertTrue(driver.getPageSource().contains("Панель управления"));
        assertNotNull(driver.findElement(By.partialLinkText("Книги")));
        assertNotNull(driver.findElement(By.partialLinkText("Заказы")));
        assertNotNull(driver.findElement(By.linkText("Панель управления")));
    }

    @Test
    void employeeCanAddBookFromAdminBooksFormAndSeeItInTable() {
        loginAsEmployee();
        driver.findElement(By.partialLinkText("Книги")).click();
        wait.until(ExpectedConditions.titleIs("Управление книгами"));

        driver.findElement(By.cssSelector("button[data-bs-target='#addBookForm']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newTitle")));

        String title = "Selenium Admin Book " + System.nanoTime();
        driver.findElement(By.id("newTitle")).sendKeys(title);
        driver.findElement(By.id("newPublisher")).sendKeys("Selenium Admin Publisher");
        driver.findElement(By.id("newLanguage")).sendKeys("RU");
        driver.findElement(By.id("newPrice")).sendKeys("321.00");
        driver.findElement(By.id("newQuantity")).sendKeys("8");
        driver.findElement(By.cssSelector("#addBookForm button[type='submit']")).click();

        wait.until(webDriver -> webDriver.getPageSource().contains(title));
        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
        assertTrue(rows.stream().anyMatch(row -> row.getText().contains(title)));
        assertTrue(rows.stream().anyMatch(row -> row.getText().contains("Selenium Admin Publisher")));
    }

    @Test
    void employeeCanChangeOrderStatusFromOrdersTable() {
        loginAsEmployee();
        driver.findElement(By.partialLinkText("Заказы")).click();
        wait.until(ExpectedConditions.titleIs("Управление заказами"));

        driver.findElement(By.id("client")).sendKeys(testUser.getLogin());
        driver.findElement(By.cssSelector("form[action*='/admin/orders'] button[type='submit']")).click();
        wait.until(webDriver -> webDriver.getPageSource().contains(testUser.getLogin()));

        WebElement statusSelect = driver.findElement(By.cssSelector("tbody tr select[name='status']"));
        new Select(statusSelect).selectByValue("DELIVERED");
        WebElement statusButton = driver.findElement(By.cssSelector("tbody tr button[type='submit']"));
        statusButton.click();

        wait.until(webDriver -> orderDao.getById(employeeManagedOrder.getId())
                .map(order -> order.getStatus() == OrderStatus.DELIVERED)
                .orElse(false));
        assertEquals(OrderStatus.DELIVERED, orderDao.getById(employeeManagedOrder.getId()).orElseThrow().getStatus());
    }

    @Test
    void employeeCanApproveRefundForSelectedOrderItem() {
        orderDao.updateStatus(employeeManagedOrder.getId(), OrderStatus.REFUND_ATTEMPT);

        loginAsEmployee();
        driver.get(url("/orders/" + employeeManagedOrder.getId()));
        wait.until(ExpectedConditions.titleIs("Заказ"));

        List<WebElement> checkboxes = driver.findElements(By.name("itemIds"));
        assertTrue(checkboxes.size() > 0);
        checkboxes.get(0).click();
        driver.findElement(By.cssSelector("form[action*='/admin/orders/'] button[type='submit']")).click();

        wait.until(webDriver -> webDriver.getPageSource().contains("REFUND_ACCEPTED"));
        assertEquals(OrderStatus.REFUND_ACCEPTED, orderDao.getById(employeeManagedOrder.getId()).orElseThrow().getStatus());
        assertEquals(ItemStatus.RETURNED, orderItemDao.findByOrderId(employeeManagedOrder.getId()).get(0).getStatus());
    }

    private void loginAsTestUser() {
        driver.get(url("/login"));
        driver.findElement(By.id("login")).sendKeys(testUser.getLogin());
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/profile"));
    }

    private void loginAsEmployee() {
        driver.get(url("/login"));
        driver.findElement(By.id("login")).sendKeys(employeeUser.getLogin());
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/admin"));
    }

    private WebDriver createChromeDriverOrSkip() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1440,1000");
        try {
            return new ChromeDriver(options);
        } catch (WebDriverException | IllegalStateException exception) {
            throw new TestAbortedException("ChromeDriver is not available in this environment", exception);
        }
    }

    private void seedData() {
        String suffix = String.valueOf(System.nanoTime());
        testUser = new User();
        testUser.setLogin("selenium-user-" + suffix);
        testUser.setPassword("password");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setContacts("{\"email\":\"selenium-" + suffix + "@example.com\"}");
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));
        testUser.setGender(Boolean.TRUE);
        testUser.setFirstName("Selenium");
        testUser.setLastName("User");
        userDao.save(testUser);

        employeeUser = new User();
        employeeUser.setLogin("selenium-employee-" + suffix);
        employeeUser.setPassword("password");
        employeeUser.setRole(UserRole.EMPLOYEE);
        employeeUser.setContacts("{\"email\":\"employee-" + suffix + "@example.com\"}");
        employeeUser.setBirthDate(LocalDate.of(1988, 2, 2));
        employeeUser.setGender(Boolean.TRUE);
        employeeUser.setFirstName("Employee");
        employeeUser.setLastName("User");
        userDao.save(employeeUser);

        Author author = new Author();
        author.setFirstName("Selenium");
        author.setLastName("Author " + suffix);
        author.setWorks(new ArrayList<>());
        authorDao.save(author);

        Work work = new Work();
        work.setTitle("Selenium Book " + suffix);
        work.setDescription("Selenium test book");
        work.setAuthors(new ArrayList<>(List.of(author)));
        workDao.save(work);

        Cover cover = new Cover();
        cover.setCoverName("Selenium Cover " + suffix);
        coverDao.save(cover);

        testEdition = new Edition();
        testEdition.setWork(work);
        testEdition.setCover(cover);
        testEdition.setPageCount(300);
        testEdition.setPublicationDate(LocalDate.of(2024, 1, 1));
        testEdition.setPublisher("Selenium Publisher");
        testEdition.setLanguage("RU");
        testEdition.setQuantity(5);
        testEdition.setPrice(new BigDecimal("777.00"));
        editionDao.save(testEdition);

        employeeManagedOrder = new Order();
        employeeManagedOrder.setClient(testUser);
        employeeManagedOrder.setOrderDate(LocalDate.of(2024, 3, 1));
        employeeManagedOrder.setStatus(OrderStatus.IN_PROCESSING);
        employeeManagedOrder.setDeliveryAddress("Employee managed address");
        employeeManagedOrder.setItems(new ArrayList<>());
        orderDao.save(employeeManagedOrder);

        OrderItem item = new OrderItem();
        item.setOrder(employeeManagedOrder);
        item.setEdition(testEdition);
        item.setQuantity(1);
        item.setStatus(ItemStatus.DELIVERED);
        orderItemDao.save(item);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
