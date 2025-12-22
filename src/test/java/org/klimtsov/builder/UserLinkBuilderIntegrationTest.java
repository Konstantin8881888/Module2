package org.klimtsov.builder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.web.context.WebApplicationContext;
import org.klimtsov.dto.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserLinkBuilderIntegrationTest {

    @Mock
    private WebApplicationContext webApplicationContext;

    @Test
    void testAllMethodsIntegration() {
        // Создаем реальный экземпляр (не через DI, так как это тест без Spring контекста)
        UserLinkBuilder userLinkBuilder = new UserLinkBuilder();

        // Тестовые данные
        UserResponse user = new UserResponse();
        user.setId(100L);
        user.setName("Integration Test");
        user.setEmail("test@integration.com");
        user.setAge(40);
        user.setCreatedAt(Instant.now());

        // Тестируем все методы
        EntityModel<UserResponse> modelWithActions = userLinkBuilder.toModel(user, true);
        assertNotNull(modelWithActions);
        assertTrue(modelWithActions.hasLink("self"));
        assertTrue(modelWithActions.hasLink("allUsers"));
        assertTrue(modelWithActions.hasLink("update-user"));
        assertTrue(modelWithActions.hasLink("delete-user"));

        EntityModel<UserResponse> modelWithoutActions = userLinkBuilder.toModel(user, false);
        assertNotNull(modelWithoutActions);
        assertTrue(modelWithoutActions.hasLink("self"));
        assertTrue(modelWithoutActions.hasLink("allUsers"));
        assertFalse(modelWithoutActions.hasLink("update-user"));
        assertFalse(modelWithoutActions.hasLink("delete-user"));

        EntityModel<UserResponse> collectionModel = userLinkBuilder.toCollectionModel(user);
        assertNotNull(collectionModel);
        assertTrue(collectionModel.hasLink("self"));

        // Тестируем методы получения отдельных ссылок
        Link selfLink = userLinkBuilder.getUserSelfLink(100L);
        assertNotNull(selfLink);
        assertEquals("self", selfLink.getRel().value());

        Link allUsersLink = userLinkBuilder.getAllUsersLink();
        assertNotNull(allUsersLink);
        assertEquals("allUsers", allUsersLink.getRel().value());

        Link updateLink = userLinkBuilder.getUpdateLink(100L);
        assertNotNull(updateLink);
        assertEquals("update-user", updateLink.getRel().value());

        Link deleteLink = userLinkBuilder.getDeleteLink(100L);
        assertNotNull(deleteLink);
        assertEquals("delete-user", deleteLink.getRel().value());

        Link createLink = userLinkBuilder.getCreateLink();
        assertNotNull(createLink);
        assertEquals("create-user", createLink.getRel().value());

        // Тестируем DTO методы
        UserResponseWithLinks responseWithLinks = userLinkBuilder.toUserResponseWithLinks(user, true);
        assertNotNull(responseWithLinks);
        assertEquals(100L, responseWithLinks.getId());
        assertNotNull(responseWithLinks.getLinks());
        assertEquals("/api/users/100", responseWithLinks.getLinks().getSelf());
        assertEquals("/api/users", responseWithLinks.getLinks().getAllUsers());
        assertEquals("/api/users", responseWithLinks.getLinks().getCreate());
        assertEquals("/api/users/100", responseWithLinks.getLinks().getUpdate());
        assertEquals("/api/users/100", responseWithLinks.getLinks().getDelete());

        UserLinks userLinks = userLinkBuilder.buildUserLinks(user, false);
        assertNotNull(userLinks);
        assertEquals("/api/users/100", userLinks.getSelf());
        assertEquals("/api/users", userLinks.getAllUsers());
        assertEquals("/api/users", userLinks.getCreate());
        assertNull(userLinks.getUpdate());
        assertNull(userLinks.getDelete());
    }

    @Test
    void testNullSafety() {
        UserLinkBuilder userLinkBuilder = new UserLinkBuilder();

        // Проверяем, что методы корректно обрабатывают null
        assertThrows(IllegalArgumentException.class, () ->
                userLinkBuilder.toModel(null, true));

        assertThrows(IllegalArgumentException.class, () ->
                userLinkBuilder.toCollectionModel(null));

        assertThrows(IllegalArgumentException.class, () ->
                userLinkBuilder.toUserResponseWithLinks(null, true));

        assertThrows(IllegalArgumentException.class, () ->
                userLinkBuilder.buildUserLinks(null, true));

        // Проверяем пользователя с null ID
        UserResponse userWithNullId = new UserResponse();
        userWithNullId.setId(null);
        userWithNullId.setName("Test");

        assertThrows(IllegalArgumentException.class, () ->
                userLinkBuilder.toModel(userWithNullId, true));

        assertThrows(IllegalArgumentException.class, () ->
                userLinkBuilder.toCollectionModel(userWithNullId));

        assertThrows(IllegalArgumentException.class, () ->
                userLinkBuilder.toUserResponseWithLinks(userWithNullId, true));

        assertThrows(IllegalArgumentException.class, () ->
                userLinkBuilder.buildUserLinks(userWithNullId, true));
    }
}