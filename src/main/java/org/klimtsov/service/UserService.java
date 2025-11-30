package org.klimtsov.service;

import org.klimtsov.dao.UserDao;
import org.klimtsov.userservice.model.User;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Long createUser(User user) {
        validateUser(user);
        return userDao.create(user);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public Optional<User> getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
        return userDao.findById(id);
    }

    public void updateUser(User user) {
        if (user.getId() == null || user.getId() <= 0) {
            throw new IllegalArgumentException("User ID must be positive for update");
        }
        validateUser(user);
        userDao.update(user);
    }

    public boolean deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
        return userDao.delete(id);
    }

    private void validateUser(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be empty");
        }
        if (user.getAge() != null && (user.getAge() < 0 || user.getAge() > 120)) {
            throw new IllegalArgumentException("Age must be between 0 and 120");
        }
    }
}