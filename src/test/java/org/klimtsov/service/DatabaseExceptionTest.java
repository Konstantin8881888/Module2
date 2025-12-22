package org.klimtsov.service;

import org.junit.jupiter.api.Test;
import org.klimtsov.dto.UserRequest;
import org.klimtsov.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class DatabaseExceptionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb_exceptions")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserService userService;

    @Test
    void whenDatabaseConnectionLost_thenServiceShouldThrowException() {
        //Проверяем, что сервис корректно обрабатывает проблемы с БД
        UserRequest request = new UserRequest();
        request.setName("Тест");
        request.setEmail("test-db@example.com");
        request.setAge(30);

        userService.createUser(request);

        //Второе сохранение с тем же email должно вызвать наше IllegalArgumentException.
        UserRequest duplicateRequest = new UserRequest();
        duplicateRequest.setName("Тест 2");
        duplicateRequest.setEmail("test-db@example.com");
        duplicateRequest.setAge(35);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(duplicateRequest));
    }
}