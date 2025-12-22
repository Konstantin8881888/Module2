package org.klimtsov.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Набор HATEOAS-ссылок для коллекции")
public class CollectionSelfCreateLinks {
    @Schema(description = "Ссылка на саму коллекцию")
    @JsonProperty("self")
    private UsersCollectionResponse.Link self;

    @Schema(description = "Ссылка для создания нового элемента")
    @JsonProperty("create")
    private UsersCollectionResponse.Link create;
}