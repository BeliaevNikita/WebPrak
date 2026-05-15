package ru.msu.cmc.webprak.web;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
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

@SpringBootTest(classes = WebPrakApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractSeleniumWebTest {

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
    protected AuthorDao authorDao;

    @Autowired
    protected CoverDao coverDao;

    @Autowired
    protected EditionDao editionDao;

    @Autowired
    protected OrderDao orderDao;

    @Autowired
    protected OrderItemDao orderItemDao;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected WorkDao workDao;

    @PersistenceContext
    protected EntityManager entityManager;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Edition testEdition;
    protected User testUser;
    protected User employeeUser;
    protected Order deliveredOrder;
    protected Order employeeManagedOrder;

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

    protected void loginAsTestUser() {
        driver.get(url("/login"));
        driver.findElement(By.id("login")).sendKeys(testUser.getLogin());
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/profile"));
    }

    protected void loginAsEmployee() {
        driver.get(url("/login"));
        driver.findElement(By.id("login")).sendKeys(employeeUser.getLogin());
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/admin"));
    }

    protected WebElement rowContaining(String text) {
        return driver.findElement(By.xpath("//tbody/tr[contains(., '" + text + "')]"));
    }

    protected WebElement rowForOrderId(Integer orderId) {
        return driver.findElement(By.xpath("//tbody/tr[td[1][normalize-space()='" + orderId + "']]"));
    }

    protected Integer currentOrderId() {
        String[] parts = driver.getCurrentUrl().split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    protected void waitForInputValue(By locator, String expectedValue) {
        wait.until(webDriver -> {
            try {
                return expectedValue.equals(webDriver.findElement(locator).getAttribute("value"));
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
    }

    protected void waitForAnyElementText(By locator, String expectedText) {
        wait.until(webDriver -> {
            try {
                return webDriver.findElements(locator).stream()
                        .map(WebElement::getText)
                        .anyMatch(text -> text.contains(expectedText));
            } catch (WebDriverException ignored) {
                return false;
            }
        });
    }

    protected void clearPersistenceContext() {
        entityManager.clear();
    }

    protected void changeOrderStatusFromAdminTable(Integer orderId, OrderStatus status) {
        wait.until(webDriver -> {
            try {
                WebElement orderRow = rowForOrderId(orderId);
                new Select(orderRow.findElement(By.name("status"))).selectByValue(status.name());
                orderRow.findElement(By.cssSelector("form[action*='/status'] button[type='submit']")).click();
                return true;
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
    }

    protected String addBookFromAdminForm(String publisher, String quantity) {
        driver.findElement(By.cssSelector("button[data-bs-target='#addBookForm']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newTitle")));

        String title = "Selenium Admin Book " + System.nanoTime();
        driver.findElement(By.id("newTitle")).sendKeys(title);
        driver.findElement(By.id("newPublisher")).sendKeys(publisher);
        driver.findElement(By.id("newLanguage")).sendKeys("RU");
        driver.findElement(By.id("newPrice")).sendKeys("321.00");
        driver.findElement(By.id("newQuantity")).sendKeys(quantity);
        driver.findElement(By.cssSelector("#addBookForm button[type='submit']")).click();
        wait.until(webDriver -> webDriver.getPageSource().contains(title));
        return title;
    }

    protected Order createOrder(OrderStatus status, String deliveryAddress, LocalDate orderDate) {
        Order order = new Order();
        order.setClient(testUser);
        order.setOrderDate(orderDate);
        order.setStatus(status);
        order.setDeliveryAddress(deliveryAddress);
        order.setItems(new ArrayList<>());
        return orderDao.save(order);
    }

    protected void createOrderItem(Order order, ItemStatus status) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setEdition(testEdition);
        item.setQuantity(1);
        item.setStatus(status);
        orderItemDao.save(item);
    }

    protected Order createCartWithItem() {
        Order cart = createOrder(OrderStatus.DRAFT, "", LocalDate.now());
        createOrderItem(cart, null);
        return cart;
    }

    protected String url(String path) {
        return "http://localhost:" + port + path;
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

        deliveredOrder = createOrder(OrderStatus.DELIVERED, "Delivered address", LocalDate.of(2024, 4, 1));
        createOrderItem(deliveredOrder, ItemStatus.DELIVERED);

        employeeManagedOrder = createOrder(OrderStatus.IN_PROCESSING, "Employee managed address", LocalDate.of(2024, 3, 1));
        createOrderItem(employeeManagedOrder, ItemStatus.DELIVERED);
    }
}
