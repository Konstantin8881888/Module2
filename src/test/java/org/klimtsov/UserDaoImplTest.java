package org.klimtsov;

import org.junit.jupiter.api.*;
import org.klimtsov.dao.UserDao;
import org.klimtsov.dao.UserDaoImpl;
import org.klimtsov.userservice.model.User;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDaoImplTest {
    private UserDao dao;

    @BeforeAll
    public void beforeAll() {
        dao = new UserDaoImpl();
    }

    @Test
    public void createFindDelete() {
        User u = new User(null, "testuser", "test@example.com", 30, Instant.now());
        Long id = dao.create(u);
        assertNotNull(id);

        Optional<User> found = dao.findById(id);
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getName());

        boolean deleted = dao.delete(id);
        assertTrue(deleted);

        Optional<User> after = dao.findById(id);
        assertFalse(after.isPresent());
    }

    @AfterAll
    public void afterAll() {
        HibernateUtil.shutdown();
    }
}
