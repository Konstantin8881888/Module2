package org.klimtsov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.klimtsov.builder.UserLinkBuilder;
import org.klimtsov.dto.*;
import org.klimtsov.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "CRUD API для управления пользователями")
public class UserController {

    private final UserService userService;
    private final UserLinkBuilder userLinkBuilder;

    @Operation(
            summary = "Создать нового пользователя",
            description = "Создание пользователя с отправкой события CREATE в Kafka"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseWithLinks.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные пользователя",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера или проблема с подключением к Kafka",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<UserResponseWithLinks> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse createdUser = userService.createUser(userRequest);

        UserResponseWithLinks response = new UserResponseWithLinks();
        response.setId(createdUser.getId());
        response.setName(createdUser.getName());
        response.setEmail(createdUser.getEmail());
        response.setAge(createdUser.getAge());
        response.setCreatedAt(createdUser.getCreatedAt());

        UserLinks links = new UserLinks();
        links.setSelf("/api/users/" + createdUser.getId());
        links.setUpdate("/api/users/" + createdUser.getId());
        links.setDelete("/api/users/" + createdUser.getId());
        links.setAllUsers("/api/users");
        links.setCreate("/api/users");
        response.setLinks(links);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Получить список всех пользователей",
            description = "Возвращает коллекцию всех пользователей"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешно получен список пользователей",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsersCollectionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера или проблема с подключением к Kafka",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<UsersCollectionResponse> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();

        List<UserResponseWithLinks> userListWithLinks = users.stream()
                .map(user -> {
                    UserResponseWithLinks response = new UserResponseWithLinks();
                    response.setId(user.getId());
                    response.setName(user.getName());
                    response.setEmail(user.getEmail());
                    response.setAge(user.getAge());
                    response.setCreatedAt(user.getCreatedAt());

                    UserLinks links = new UserLinks();
                    links.setSelf("/api/users/" + user.getId());
                    links.setCreate("/api/users");
                    response.setLinks(links);

                    return response;
                })
                .collect(Collectors.toList());

        UsersCollectionResponse.Embedded embedded =
                new UsersCollectionResponse.Embedded(userListWithLinks);

        CollectionSelfCreateLinks collectionLinks = new CollectionSelfCreateLinks();
        collectionLinks.setSelf("/api/users");
        collectionLinks.setCreate("/api/users");

        UsersCollectionResponse response = new UsersCollectionResponse(embedded, collectionLinks);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Получение информации о пользователе по идентификатору."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseWithLinks.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат ID (не числовой)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseWithLinks> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        UserResponseWithLinks response = userLinkBuilder.toUserResponseWithLinks(user, true);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Обновить данные пользователя",
            description = "Обновление информации о существующем пользователе."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно обновлен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseWithLinks.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные для обновления",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseWithLinks> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        UserResponseWithLinks response = userLinkBuilder.toUserResponseWithLinks(updatedUser, true);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Удалить пользователя",
            description = "Удаление пользователя из системы по идентификатору."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно удален",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeleteResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера или проблема с подключением к Kafka",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        DeleteLinks deleteLinks = new DeleteLinks();
        deleteLinks.setAllUsers("/api/users");
        deleteLinks.setCreate("/api/users");  // ✅ Добавляем create ссылку

        DeleteResponse response = new DeleteResponse(
                "Пользователь с ID " + id + " успешно удален",
                Instant.now().toString(),
                deleteLinks
        );

        return ResponseEntity.ok(response);
    }
}