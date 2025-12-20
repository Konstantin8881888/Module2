package org.klimtsov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.klimtsov.builder.UserLinkBuilder;
import org.klimtsov.dto.UserRequest;
import org.klimtsov.dto.UserResponse;
import org.klimtsov.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserLinkBuilder userLinkBuilder;

    @PostMapping
    public ResponseEntity<EntityModel<UserResponse>> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse createdUser = userService.createUser(userRequest);
        EntityModel<UserResponse> entityModel = userLinkBuilder.toModel(createdUser, true);

        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();

        List<EntityModel<UserResponse>> userModels = users.stream()
                .map(userLinkBuilder::toCollectionModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UserResponse>> collectionModel = CollectionModel.of(userModels);

        collectionModel.add(
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel(),
                userLinkBuilder.getCreateLink()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        EntityModel<UserResponse> entityModel = userLinkBuilder.toModel(user, true);

        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        EntityModel<UserResponse> entityModel = userLinkBuilder.toModel(updatedUser, true);

        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModel<Map<String, String>>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Пользователь с ID " + id + " успешно удален");
        response.put("timestamp", Instant.now().toString());

        EntityModel<Map<String, String>> entityModel = EntityModel.of(response);
        entityModel.add(
                userLinkBuilder.getAllUsersLink(),
                userLinkBuilder.getCreateLink()
        );

        return ResponseEntity.ok(entityModel);
    }
}