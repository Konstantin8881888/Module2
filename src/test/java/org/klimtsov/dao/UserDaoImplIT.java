package org.klimtsov.dao;

import org.junit.jupiter.api.*;
import org.klimtsov.userservice.model.User;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDaoImplIT {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImplIT.class);

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true);

    private UserDao userDao;
    private SessionFactory sessionFactory;

    @BeforeAll
    void setup() {
        logger.info("Starting PostgreSQL container...");
        logger.info("JDBC URL: {}", postgres.getJdbcUrl());
        logger.info("Username: {}", postgres.getUsername());
        logger.info("Password: {}", postgres.getPassword());

        //Даем контейнеру время полностью запуститься.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Настройка.
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.setProperty("hibernate.connection.pool_size", "10");
        configuration.setProperty("hibernate.hikari.maximumPoolSize", "10");

        configuration.addAnnotatedClass(User.class);

        try {
            sessionFactory = configuration.buildSessionFactory();
            userDao = new UserDaoImpl(sessionFactory);
            logger.info("SessionFactory created successfully");
        } catch (Exception e) {
            logger.error("Failed to create SessionFactory", e);
            throw new RuntimeException("Failed to initialize test database", e);
        }
    }

    @BeforeEach
    void cleanDatabase() {
        logger.debug("Cleaning database before test");
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                session.createMutationQuery("DELETE FROM User").executeUpdate();
                transaction.commit();
                logger.debug("Database cleaned successfully");
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Failed to clean database", e);
                throw e;
            }
        }
    }

    @AfterAll
    void tearDown() {
        logger.info("Closing resources...");
        if (sessionFactory != null) {
            try {
                sessionFactory.close();
                logger.info("SessionFactory closed successfully");
            } catch (Exception e) {
                logger.error("Error closing SessionFactory", e);
            }
        }
    }

    @Test
    void createUser_ShouldReturnGeneratedId() {
        User user = new User(null, "Test User", "test@example.com", 25, Instant.now());

        Long id = userDao.create(user);

        assertNotNull(id);
        assertTrue(id > 0);
        logger.info("Created user with ID: {}", id);
    }

    @Test
    void findById_WithExistingUser_ShouldReturnUser() {
        User user = new User(null, "Test User", "test@example.com", 25, Instant.now());
        Long id = userDao.create(user);

        Optional<User> found = userDao.findById(id);

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals(25, found.get().getAge());
    }

    @Test
    void findById_WithNonExistingUser_ShouldReturnEmpty() {
        Optional<User> found = userDao.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_WithUsers_ShouldReturnAllUsers() {
        userDao.create(new User(null, "User1", "user1@example.com", 25, Instant.now()));
        userDao.create(new User(null, "User2", "user2@example.com", 30, Instant.now()));

        List<User> users = userDao.findAll();

        assertEquals(2, users.size());
        logger.info("Found {} users", users.size());
    }

    @Test
    void findAll_WithNoUsers_ShouldReturnEmptyList() {
        List<User> users = userDao.findAll();

        assertTrue(users.isEmpty());
    }

    @Test
    void update_WithExistingUser_ShouldUpdateUser() {
        User user = new User(null, "Original", "original@example.com", 25, Instant.now());
        Long id = userDao.create(user);

        user.setId(id);
        user.setName("Updated");
        user.setEmail("updated@example.com");
        user.setAge(30);

        userDao.update(user);

        Optional<User> updated = userDao.findById(id);
        assertTrue(updated.isPresent());
        assertEquals("Updated", updated.get().getName());
        assertEquals("updated@example.com", updated.get().getEmail());
        assertEquals(30, updated.get().getAge());
    }

    @Test
    void delete_WithExistingUser_ShouldReturnTrue() {
        User user = new User(null, "To Delete", "delete@example.com", 25, Instant.now());
        Long id = userDao.create(user);

        boolean deleted = userDao.delete(id);

        assertTrue(deleted);
        assertFalse(userDao.findById(id).isPresent());
    }

    @Test
    void delete_WithNonExistingUser_ShouldReturnFalse() {
        boolean deleted = userDao.delete(999L);

        assertFalse(deleted);
    }
}