package org.klimtsov.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseWithLinks {
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private Instant createdAt;

    @JsonProperty("_links")
    private UserLinks links;
}