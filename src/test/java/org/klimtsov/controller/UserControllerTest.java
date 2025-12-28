package org.klimtsov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.klimtsov.dto.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@AutoConfigureMockMvc
@Testcontainers
@Transactional //Все тесты выполняются в транзакции и откатываются автоматически.
class UserControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
    )
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.jpa.show-sql", () -> "true");

        //Для работы транзакций.
        registry.add("spring.jpa.open-in-view", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_ValidRequest_ReturnsCreatedUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Иван Иванов");
        userRequest.setEmail("ivan@example.com");
        userRequest.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Иван Иванов");
        userRequest.setEmail("invalid-email");
        userRequest.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void createUser_InvalidAge_ReturnsBadRequest() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Иван Иванов");
        userRequest.setEmail("ivan@example.com");
        userRequest.setAge(150);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void createUser_EmptyName_ReturnsBadRequest() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("");
        userRequest.setEmail("ivan@example.com");
        userRequest.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void createUser_DuplicateEmail_ReturnsBadRequest() throws Exception {
        //Создаем первого пользователя.
        UserRequest firstUser = new UserRequest();
        firstUser.setName("Иван Иванов");
        firstUser.setEmail("duplicate@example.com");
        firstUser.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        //Пытаемся создать второго пользователя с таким же email.
        UserRequest secondUser = new UserRequest();
        secondUser.setName("Петр Петров");
        secondUser.setEmail("duplicate@example.com");
        secondUser.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Пользователь с email duplicate@example.com уже существует"))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() throws Exception {
        UserRequest user1 = new UserRequest();
        user1.setName("Иван Иванов");
        user1.setEmail("ivan@example.com");
        user1.setAge(25);

        UserRequest user2 = new UserRequest();
        user2.setName("Петр Петров");
        user2.setEmail("petr@example.com");
        user2.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$._embedded.userList[0].name").value("Иван Иванов"))
                .andExpect(jsonPath("$._embedded.userList[1].name").value("Петр Петров"));
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Иван Иванов");
        userRequest.setEmail("ivan@example.com");
        userRequest.setAge(25);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void getUserById_NonExistingUser_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с ID 999 не найден"))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void updateUser_ValidRequest_ReturnsUpdatedUser() throws Exception {
        UserRequest createRequest = new UserRequest();
        createRequest.setName("Старое Имя");
        createRequest.setEmail("old@example.com");
        createRequest.setAge(25);

        String createResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Новое Имя");
        updateRequest.setEmail("new@example.com");
        updateRequest.setAge(30);

        mockMvc.perform(put("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое Имя"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.age").value(30));
    }

    @Test
    void updateUser_WithDuplicateEmail_ReturnsBadRequest() throws Exception {
        UserRequest firstUser = new UserRequest();
        firstUser.setName("Первый Пользователь");
        firstUser.setEmail("first@example.com");
        firstUser.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        UserRequest secondUser = new UserRequest();
        secondUser.setName("Второй Пользователь");
        secondUser.setEmail("second@example.com");
        secondUser.setAge(30);

        String secondResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long secondUserId = objectMapper.readTree(secondResponse).get("id").asLong();

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Обновленный");
        updateRequest.setEmail("first@example.com"); // Email первого пользователя
        updateRequest.setAge(35);

        mockMvc.perform(put("/api/users/" + secondUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Пользователь с email first@example.com уже существует"))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void deleteUser_ExistingUser_ReturnsNoContent() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Пользователь для удаления");
        userRequest.setEmail("delete@example.com");
        userRequest.setAge(25);

        String createResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_NonExistingUser_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с ID 999 не найден"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    //Проверка граничных значений.
    @Test
    void createUser_AgeMinValue_ReturnsCreatedUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Иван Иванов");
        userRequest.setEmail("minage@example.com");
        userRequest.setAge(0); // Минимальный возраст

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.age").value(0));
    }

    @Test
    void createUser_AgeMaxValue_ReturnsCreatedUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Иван Иванов");
        userRequest.setEmail("maxage@example.com");
        userRequest.setAge(120); // Максимальный возраст

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.age").value(120));
    }

    @Test
    void createUser_NullAge_ReturnsCreatedUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Иван Иванов");
        userRequest.setEmail("nullage@example.com");
        userRequest.setAge(null); // Возраст не указан

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.age").doesNotExist());
    }

    @Test
    void updateUser_PartialUpdate_ReturnsUpdatedUser() throws Exception {
        UserRequest createRequest = new UserRequest();
        createRequest.setName("Иван Иванов");
        createRequest.setEmail("ivan@example.com");
        createRequest.setAge(25);

        String createResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        //Обновляем только имя (не отправляем email и возраст).
        String partialUpdateJson = """
        {
            "name": "Новое Имя"
        }
        """;

        mockMvc.perform(put("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialUpdateJson))
                .andExpect(status().isBadRequest()); //Должна быть ошибка валидации.
    }

    @Test
    void queryWithInvalidSQL_ReturnsInternalServerError() throws Exception {
        //Обработка RuntimeException, которые могут включать SQLGrammarException.
        UserRequest validUser = new UserRequest();
        validUser.setName("Тест");
        validUser.setEmail("test@example.com");
        validUser.setAge(25);

        //Обычный запрос.
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated());

        //Несуществующий путь.
        mockMvc.perform(get("/api/users/invalid-path"))
                .andExpect(status().isBadRequest()); //Должен вернуть 400, а не 500.
    }

    @Test
    void createUser_WithNullValues_ReturnsBadRequest() throws Exception {
        String invalidJson = """
        {
            "name": null,
            "email": null,
            "age": null
        }
        """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
    }
}