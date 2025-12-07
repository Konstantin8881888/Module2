package org.klimtsov.repository;

import org.klimtsov.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //Spring автоматически создаст CRUD методы.
    boolean existsByEmail(String email);
}