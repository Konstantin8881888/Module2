package org.klimtsov.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Набор HATEOAS-ссылок для пользователя")
public class UserLinks {
    @Schema(description = "Ссылка на самого себя", example = "/api/users/1")
    @JsonProperty("self")
    private String self;

    @Schema(description = "Ссылка для обновления", example = "/api/users/1")
    @JsonProperty("update")
    private String update;

    @Schema(description = "Ссылка для удаления", example = "/api/users/1")
    @JsonProperty("delete")
    private String delete;

    @Schema(description = "Ссылка на список всех пользователей", example = "/api/users")
    @JsonProperty("allUsers")
    private String allUsers;

    @Schema(description = "Ссылка для создания нового пользователя", example = "/api/users")
    @JsonProperty("create")
    private String create;
}