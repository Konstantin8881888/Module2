package org.klimtsov.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с коллекцией пользователей и HATEOAS-ссылками")
public class UsersCollectionResponse {
    @JsonProperty("_embedded")
    @Schema(description = "Вложенные данные")
    private Embedded embedded;

    @JsonProperty("_links")
    private CollectionSelfCreateLinks links;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Вложенный объект с пользователями")
    public static class Embedded {
        @Schema(description = "Список пользователей с ссылками")
        private List<UserResponseWithLinks> userList;
    }
}