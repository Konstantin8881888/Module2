package org.klimtsov.service;

import lombok.RequiredArgsConstructor;
import org.klimtsov.dto.UserEvent;
import org.klimtsov.dto.UserRequest;
import org.klimtsov.dto.UserResponse;
import org.klimtsov.exception.UserAlreadyExistsException;
import org.klimtsov.exception.UserNotFoundException;
import org.klimtsov.kafka.KafkaProducer;
import org.klimtsov.repository.UserRepository;
import org.klimtsov.userservice.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducer;

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        //Проверяем, существует ли email.
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException(userRequest.getEmail());
        }

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());
        user.setCreatedAt(Instant.now());

        User savedUser = userRepository.save(user);
        UserEvent event = new UserEvent(savedUser.getEmail(), "CREATE");
        kafkaProducer.sendUserEvent(event);
        return convertToResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return convertToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        //Проверяем, что email не занят.
        if (!user.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException(userRequest.getEmail());
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        UserEvent event = new UserEvent(user.getEmail(), "DELETE");
        kafkaProducer.sendUserEvent(event);

        userRepository.deleteById(id);
    }

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }
}