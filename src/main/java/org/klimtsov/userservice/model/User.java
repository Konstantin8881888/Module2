package org.klimtsov.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Имя не может быть пустым!")
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    @NotBlank(message = "Email не может быть пустым!")
    @Email(message = "Некорректный формат email!")
    private String email;

    @Column(name = "age")
    @Min(value = 0, message = "Возраст должен быть от 0 до 120!")
    @Max(value = 120, message = "Возраст должен быть от 0 до 120!")
    private Integer age;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}