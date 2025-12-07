package org.klimtsov.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @Min(value = 0, message = "Возраст должен быть от 0 до 120")
    @Max(value = 120, message = "Возраст должен быть от 0 до 120")
    private Integer age;
}