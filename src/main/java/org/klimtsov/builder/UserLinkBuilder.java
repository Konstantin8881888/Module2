package org.klimtsov.builder;

import org.klimtsov.controller.UserController;
import org.klimtsov.dto.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserLinkBuilder {

    //Статический пустой объект для генерации ссылок.
    private static final UserRequest EMPTY_REQUEST;

    static {
        EMPTY_REQUEST = new UserRequest();
        EMPTY_REQUEST.setName("");
        EMPTY_REQUEST.setEmail("");
        EMPTY_REQUEST.setAge(null);
    }

    public EntityModel<UserResponse> toModel(UserResponse user, boolean includeActionLinks) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User и его ID не могут быть null");
        }

        Long userId = user.getId();
        List<Link> links = new ArrayList<>();

        links.add(linkTo(methodOn(UserController.class).getUserById(userId)).withSelfRel());
        links.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("allUsers"));

        if (includeActionLinks) {
            //Используем EMPTY_REQUEST вместо null для надёжности.
            links.add(linkTo(methodOn(UserController.class)
                    .updateUser(userId, EMPTY_REQUEST)).withRel("update-user").withType("PUT"));
            links.add(linkTo(methodOn(UserController.class)
                    .deleteUser(userId)).withRel("delete-user").withType("DELETE"));
        }

        return EntityModel.of(user, links);
    }

    //EntityModel для пользователя с кастомным набором ссылок.
    public EntityModel<UserResponse> toModel(UserResponse user, Link... additionalLinks) {
        EntityModel<UserResponse> model = toModel(user, false);
        if (additionalLinks != null) {
            model.add(additionalLinks);
        }
        return model;
    }

    //Упрощённая модель для элементов коллекции.
    public EntityModel<UserResponse> toCollectionModel(UserResponse user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User и его ID не могут быть null");
        }

        Long userId = user.getId();
        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).getUserById(userId)).withSelfRel()
        );
    }

    public Link getUserSelfLink(Long userId) {
        return linkTo(methodOn(UserController.class).getUserById(userId)).withSelfRel();
    }

    public Link getAllUsersLink() {
        return linkTo(methodOn(UserController.class).getAllUsers()).withRel("allUsers");
    }

    public Link getUpdateLink(Long userId) {
        return linkTo(methodOn(UserController.class)
                .updateUser(userId, EMPTY_REQUEST))
                .withRel("update-user") //Более логичное имя.
                .withType("PUT");
    }

    public Link getDeleteLink(Long userId) {
        return linkTo(methodOn(UserController.class)
                .deleteUser(userId))
                .withRel("delete-user")
                .withType("DELETE");
    }

    public Link getCreateLink() {
        return linkTo(methodOn(UserController.class)
                .createUser(EMPTY_REQUEST))
                .withRel("create-user")
                .withType("POST");
    }

    public UserResponseWithLinks toUserResponseWithLinks(UserResponse user, boolean includeActionLinks) {
        UserResponseWithLinks response = new UserResponseWithLinks();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAge(user.getAge());
        response.setCreatedAt(user.getCreatedAt());

        UserLinks links = buildUserLinks(user, includeActionLinks);
        response.setLinks(links);

        return response;
    }

    public UserLinks buildUserLinks(UserResponse user, boolean includeActionLinks) {
        UserLinks userLinks = new UserLinks();

        userLinks.setSelf("/api/users/" + user.getId());
        userLinks.setAllUsers("/api/users");
        userLinks.setCreate("/api/users");

        if (includeActionLinks) {
            userLinks.setUpdate("/api/users/" + user.getId());
            userLinks.setDelete("/api/users/" + user.getId());
        }

        return userLinks;
    }
}