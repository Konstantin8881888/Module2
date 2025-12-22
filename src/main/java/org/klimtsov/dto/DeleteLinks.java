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
    @Schema(description = "Ссылка на список всех пользователей")
    @JsonProperty("allUsers")
    private UsersCollectionResponse.Link allUsers;

    @Schema(description = "Ссылка для создания нового пользователя")
    @JsonProperty("create")
    private UsersCollectionResponse.Link create;
}