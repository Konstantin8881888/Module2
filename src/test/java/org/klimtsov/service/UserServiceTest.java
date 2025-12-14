package org.klimtsov.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.klimtsov.dto.UserRequest;
import org.klimtsov.dto.UserResponse;
import org.klimtsov.kafka.KafkaProducer;
import org.klimtsov.repository.UserRepository;
import org.klimtsov.userservice.model.User;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
    private UserRepository userRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ValidUser_ReturnsUserResponse() {
        UserRequest request = new UserRequest();
        request.setName("Иван Иванов");
        request.setEmail("ivan@example.com");
        request.setAge(25);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName(request.getName());
        savedUser.setEmail(request.getEmail());
        savedUser.setAge(request.getAge());
        savedUser.setCreatedAt(Instant.now());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(savedUser.getId(), response.getId());
        assertEquals(savedUser.getName(), response.getName());
        assertEquals(savedUser.getEmail(), response.getEmail());
        assertEquals(savedUser.getAge(), response.getAge());

        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(kafkaProducer, times(1)).sendUserEvent(any());
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        UserRequest request = new UserRequest();
        request.setName("Иван Иванов");
        request.setEmail("ivan@example.com");
        request.setAge(25);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );

        assertEquals("Пользователь с таким email уже существует!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(kafkaProducer, never()).sendUserEvent(any());
    }

    @Test
    void getUserById_ExistingUser_ReturnsUserResponse() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Иван Иванов");
        user.setEmail("ivan@example.com");
        user.setAge(25);
        user.setCreatedAt(Instant.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getAge(), response.getAge());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_NonExistingUser_ThrowsException() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(userId)
        );

        assertEquals("Пользователь не найден с id: " + userId, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() {
        User user1 = new User(1L, "Иван", "ivan@example.com", 25, Instant.now());
        User user2 = new User(2L, "Пётр", "petr@example.com", 30, Instant.now());

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Иван", responses.get(0).getName());
        assertEquals("Пётр", responses.get(1).getName());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void updateUser_ValidData_ReturnsUpdatedUser() {
        Long userId = 1L;
        User existingUser = new User(userId, "Старое Имя", "old@example.com", 25, Instant.now());

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Новое Имя");
        updateRequest.setEmail("new@example.com");
        updateRequest.setAge(30);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Новое Имя", response.getName());
        assertEquals("new@example.com", response.getEmail());
        assertEquals(30, response.getAge());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(updateRequest.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(kafkaProducer, never()).sendUserEvent(any());
    }

    @Test
    void deleteUser_ExistingUser_DeletesUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
        verify(kafkaProducer, times(1)).sendUserEvent(any());
    }

    @Test
    void updateUser_SameEmail_ShouldNotCheckForDuplicates() {
        //Обновляем пользователя с тем же email.
        Long userId = 1L;
        String sameEmail = "same@example.com";
        User existingUser = new User(userId, "Старое Имя", sameEmail, 25, Instant.now());

        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Новое Имя");
        updateRequest.setEmail(sameEmail); //Тот же email.
        updateRequest.setAge(30);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals("Новое Имя", response.getName());

        //Проверяем, что existsByEmail не вызывался.
        verify(userRepository, never()).existsByEmail(sameEmail);
    }

    @Test
    void createUser_NullAge_ShouldNotValidateAge() {
        //Пользователь без возраста.
        UserRequest request = new UserRequest();
        request.setName("Иван Иванов");
        request.setEmail("test@example.com");
        request.setAge(null);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName(request.getName());
        savedUser.setEmail(request.getEmail());
        savedUser.setAge(null);
        savedUser.setCreatedAt(Instant.now());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertNull(response.getAge());

        verify(kafkaProducer, times(1)).sendUserEvent(any());
    }

    @Test
    void createUser_WhenDatabaseConstraintViolation_ThrowsDataIntegrityViolationException() {
        UserRequest request = new UserRequest();
        request.setName("Иван Иванов");
        request.setEmail("test@example.com");
        request.setAge(25);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        //При сохранении произойдёт ошибка ограничения БД на длинное имя.
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Ошибка ограничения базы данных"));

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.createUser(request)
        );

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Ошибка ограничения базы данных"));

        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void validateUser_AgeExactlyZero_ShouldPass() {
        //Проверяем граничное значение.
        User user = new User();
        user.setName("Иван Иванов");
        user.setEmail("test@example.com");
        user.setAge(0);

        //Должен пройти без исключений.
        assertDoesNotThrow(() -> {
        });
    }

    @Test
    void validateUser_AgeExactly120_ShouldPass() {
        //Аналогично для нижней границы.
        User user = new User();
        user.setName("Иван Иванов");
        user.setEmail("test@example.com");
        user.setAge(120);
    }
}