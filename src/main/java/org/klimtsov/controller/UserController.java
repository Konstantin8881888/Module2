package org.klimtsov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.klimtsov.builder.UserLinkBuilder;
import org.klimtsov.dto.*;
import org.klimtsov.service.UserService;
import org.springframework.hateoas.EntityModel;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
                            schema = @Schema(implementation = UserResponse.class)
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
    @ResponseBody
    public ResponseEntity<EntityModel<UserResponse>> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse createdUser = userService.createUser(userRequest);
        EntityModel<UserResponse> entityModel = userLinkBuilder.toModel(createdUser, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
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
    @ResponseBody
    public ResponseEntity<UsersCollectionResponse> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();

        List<UserResponseWithLinks> userListWithLinks = users.stream()
                .map(user -> userLinkBuilder.toUserResponseWithLinks(user, false))
                .collect(Collectors.toList());

        UsersCollectionResponse.Embedded embedded =
                new UsersCollectionResponse.Embedded(userListWithLinks);

        CollectionLinks collectionLinks = new CollectionLinks();
        collectionLinks.setSelf(userLinkBuilder.convertToDtoLink(
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel()));
        collectionLinks.setCreate(userLinkBuilder.convertToDtoLink(
                linkTo(methodOn(UserController.class).createUser(null)).withRel("create")));

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
    @ResponseBody
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
    @ResponseBody
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

        CollectionLinks deleteLinks = new CollectionLinks();
        deleteLinks.setAllUsers(userLinkBuilder.convertToDtoLink(userLinkBuilder.getAllUsersLink()));
        deleteLinks.setCreate(userLinkBuilder.convertToDtoLink(userLinkBuilder.getCreateLink()));

        DeleteResponse response = new DeleteResponse(
                "Пользователь с ID " + id + " успешно удален",
                Instant.now().toString(),
                deleteLinks
        );

        return ResponseEntity.ok(response);
    }
}