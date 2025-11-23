package org.klimtsov.dao;

import org.klimtsov.userservice.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Long create(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void update(User user);
    boolean delete(Long id);
}
