package ru.msu.cmc.webprak.dao;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.msu.cmc.webprak.WebPrakApplication;

@SpringBootTest(classes = WebPrakApplication.class)
@Transactional
public abstract class AbstractDaoIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("book_store_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }

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

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @AfterEach
    void flushPersistenceContext() {
        // Transaction rollback is enough for cleanup; this hook is a stable place for future flush checks.
    }
}
