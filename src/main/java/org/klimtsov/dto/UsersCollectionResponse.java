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
    private CollectionLinks links;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Вложенный объект с пользователями")
    public static class Embedded {
        @Schema(description = "Список пользователей с ссылками")
        private List<UserResponseWithLinks> userList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    //Определяем схему для Link как компонент, чтобы переиспользовать.
    @Schema(name = "Link", description = "Модель HATEOAS-ссылки")
    public static class Link {
        @Schema(description = "URL ссылки", example = "/api/users/1")
        private String href;
        @Schema(description = "Язык ссылки", example = "ru", nullable = true)
        private String hreflang;
        @Schema(description = "Заголовок ссылки", example = "Информация о пользователе", nullable = true)
        private String title;
        @Schema(description = "Тип медиа", example = "application/json", nullable = true)
        private String type;
        @Schema(description = "Признак устаревшей ссылки", nullable = true)
        private String deprecation;
        @Schema(description = "Профиль ссылки", nullable = true)
        private String profile;
        @Schema(description = "Имя ссылки", example = "self")
        private String name;
        @Schema(description = "Флаг шаблона URI", example = "false")
        private Boolean templated;
    }
}