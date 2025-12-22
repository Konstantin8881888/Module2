package org.klimtsov.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Набор HATEOAS-ссылок после удаления")
public class DeleteLinks {
    @Schema(description = "Ссылка на список всех пользователей", example = "/api/users")
    @JsonProperty("allUsers")
    private String allUsers;

    @Schema(description = "Ссылка для создания нового пользователя", example = "/api/users")
    @JsonProperty("create")
    private String create;
}