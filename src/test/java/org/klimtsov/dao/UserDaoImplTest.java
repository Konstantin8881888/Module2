package org.klimtsov.dao;

import org.junit.jupiter.api.*;
import org.klimtsov.HibernateUtil;
import org.klimtsov.userservice.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDaoImplTest {
    private UserDao dao;
    private User testUser;

    @BeforeAll
    public void beforeAll() {
        dao = new UserDaoImpl();
    }

    @BeforeEach
    public void setUp() {
        // Создаем тестового пользователя перед каждым тестом
        testUser = new User(null, "testuser", "test@example.com", 30, Instant.now());
        Long id = dao.create(testUser);
        testUser.setId(id);
    }

    @AfterEach
    public void tearDown() {
        // Очищаем базу после каждого теста
        try {
            if (testUser.getId() != null) {
                dao.delete(testUser.getId());
            }
        } catch (Exception e) {
            // Игнорируем ошибки при удалении
        }
    }

    @Test
    public void createUser_Success() {
        User newUser = new User(null, "newuser", "new@example.com", 25, Instant.now());
        Long id = dao.create(newUser);

        assertNotNull(id);
        assertTrue(id > 0);

        //Очистка.
        dao.delete(id);
    }

    @Test
    public void findById_ExistingUser_ReturnsUser() {
        Optional<User> found = dao.findById(testUser.getId());

        assertTrue(found.isPresent());
        assertEquals(testUser.getName(), found.get().getName());
        assertEquals(testUser.getEmail(), found.get().getEmail());
        assertEquals(testUser.getAge(), found.get().getAge());
    }

    @Test
    public void findById_NonExistingUser_ReturnsEmpty() {
        Optional<User> found = dao.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    public void findAll_ReturnsAllUsers() {
        //Создаем второго пользователя.
        User user2 = new User(null, "user2", "user2@example.com", 35, Instant.now());
        Long user2Id = dao.create(user2);

        List<User> allUsers = dao.findAll();

        assertFalse(allUsers.isEmpty());
        assertTrue(allUsers.size() >= 2);

        //Очистка.
        dao.delete(user2Id);
    }

    @Test
    public void updateUser_Success() {
        //Изменяем данные пользователя.
        testUser.setName("updatedname");
        testUser.setEmail("updated@example.com");
        testUser.setAge(35);

        dao.update(testUser);

        //Проверяем, что данные обновились.
        Optional<User> updated = dao.findById(testUser.getId());
        assertTrue(updated.isPresent());
        assertEquals("updatedname", updated.get().getName());
        assertEquals("updated@example.com", updated.get().getEmail());
        assertEquals(35, updated.get().getAge());
    }

    @Test
    public void deleteUser_ExistingUser_ReturnsTrue() {
        boolean deleted = dao.delete(testUser.getId());
        assertTrue(deleted);

        //Проверяем, что пользователь действительно удален.
        Optional<User> found = dao.findById(testUser.getId());
        assertFalse(found.isPresent());
    }

    @Test
    public void deleteUser_NonExistingUser_ReturnsFalse() {
        boolean deleted = dao.delete(999L);
        assertFalse(deleted);
    }

    @Test
    public void createUserWithNullFields() {
        //Некоторые поля могут быть null (в зависимости от конфигурации БД).
        User userWithNulls = new User(null, null, null, null, null);
        Long id = dao.create(userWithNulls);

        assertNotNull(id);
        assertTrue(id > 0);

        // Очистка.
        dao.delete(id);
    }

    @Test
    public void createUserWithPartialNullFields() {
        User user = new User(null, "somename", null, null, Instant.now());
        Long id = dao.create(user);

        assertNotNull(id);

        Optional<User> found = dao.findById(id);
        assertTrue(found.isPresent());
        assertEquals("somename", found.get().getName());
        assertNull(found.get().getEmail());
        assertNull(found.get().getAge());

        // Очистка
        dao.delete(id);
    }

    @AfterAll
    public void afterAll() {
        HibernateUtil.shutdown();
    }
}