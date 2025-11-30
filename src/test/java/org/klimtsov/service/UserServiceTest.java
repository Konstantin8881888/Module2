package org.klimtsov.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.klimtsov.dao.UserDao;
import org.klimtsov.userservice.model.User;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_WithValidUser_ShouldReturnId() {
        User user = new User(null, "Test User", "test@example.com", 25, Instant.now());
        when(userDao.create(any(User.class))).thenReturn(1L);

        Long id = userService.createUser(user);

        assertEquals(1L, id);
        verify(userDao, times(1)).create(user);
    }

    @Test
    void createUser_WithNullName_ShouldThrowException() {
        User user = new User(null, null, "test@example.com", 25, Instant.now());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("User name cannot be empty", exception.getMessage());
        verify(userDao, never()).create(any(User.class));
    }

    @Test
    void createUser_WithEmptyName_ShouldThrowException() {
        User user = new User(null, "  ", "test@example.com", 25, Instant.now());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("User name cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_WithInvalidAge_ShouldThrowException() {
        User user = new User(null, "Test User", "test@example.com", 150, Instant.now());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(user)
        );

        assertEquals("Age must be between 0 and 120", exception.getMessage());
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        User user = new User(1L, "Test User", "test@example.com", 25, Instant.now());
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    void getUserById_WithNullId_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(null)
        );

        assertEquals("ID must be positive", exception.getMessage());
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(-1L)
        );

        assertEquals("ID must be positive", exception.getMessage());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> users = Arrays.asList(
                new User(1L, "User1", "user1@example.com", 25, Instant.now()),
                new User(2L, "User2", "user2@example.com", 30, Instant.now())
        );
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void updateUser_WithValidUser_ShouldUpdateUser() {
        User user = new User(1L, "Updated User", "updated@example.com", 30, Instant.now());

        userService.updateUser(user);

        verify(userDao, times(1)).update(user);
    }

    @Test
    void updateUser_WithNullId_ShouldThrowException() {
        User user = new User(null, "Updated User", "updated@example.com", 30, Instant.now());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(user)
        );

        assertEquals("User ID must be positive for update", exception.getMessage());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void deleteUser_WithValidId_ShouldReturnTrue() {
        when(userDao.delete(1L)).thenReturn(true);

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userDao, times(1)).delete(1L);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(0L)
        );

        assertEquals("ID must be positive", exception.getMessage());
        verify(userDao, never()).delete(any(Long.class));
    }
}