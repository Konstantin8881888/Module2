package org.klimtsov.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.klimtsov.dto.*;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserLinkBuilderTest {

    @InjectMocks
    private UserLinkBuilder userLinkBuilder;

    @Mock
    private WebApplicationContext webApplicationContext;

    private UserResponse testUser;
    private UserResponse testUser2;

    @BeforeEach
    void setUp() {
        testUser = new UserResponse();
        testUser.setId(1L);
        testUser.setName("Иван Иванов");
        testUser.setEmail("ivan@example.com");
        testUser.setAge(25);
        testUser.setCreatedAt(Instant.now());

        testUser2 = new UserResponse();
        testUser2.setId(2L);
        testUser2.setName("Петр Петров");
        testUser2.setEmail("petr@example.com");
        testUser2.setAge(30);
        testUser2.setCreatedAt(Instant.now());
    }

    @Test
    void testToModel_WithActionLinks_ShouldReturnEntityModelWithAllLinks() {
        EntityModel<UserResponse> model = userLinkBuilder.toModel(testUser, true);

        assertNotNull(model);
        assertNotNull(model.getContent());
        assertEquals(testUser, model.getContent());

        Links links = model.getLinks();
        assertNotNull(links);

        assertTrue(links.hasLink("self"));
        assertTrue(links.hasLink("allUsers"));
        assertTrue(links.hasLink("update-user"));
        assertTrue(links.hasLink("delete-user"));
    }

    @Test
    void testToModel_WithoutActionLinks_ShouldReturnEntityModelWithBasicLinks() {
        EntityModel<UserResponse> model = userLinkBuilder.toModel(testUser, false);

        assertNotNull(model);
        assertNotNull(model.getContent());
        assertEquals(testUser, model.getContent());

        Links links = model.getLinks();
        assertNotNull(links);

        assertTrue(links.hasLink("self"));
        assertTrue(links.hasLink("allUsers"));
        assertFalse(links.hasLink("update-user"));
        assertFalse(links.hasLink("delete-user"));
    }

    @Test
    void testToModel_WithAdditionalLinks_ShouldIncludeAllLinks() {
        Link additionalLink1 = Link.of("/api/custom", "custom-action");
        Link additionalLink2 = Link.of("/api/other", "other-action");

        EntityModel<UserResponse> model = userLinkBuilder.toModel(testUser, additionalLink1, additionalLink2);

        assertNotNull(model);

        Links links = model.getLinks();
        assertNotNull(links);

        assertTrue(links.hasLink("self"));
        assertTrue(links.hasLink("allUsers"));
        assertTrue(links.hasLink("custom-action"));
        assertTrue(links.hasLink("other-action"));
    }

    @Test
    void testToModel_NullUser_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userLinkBuilder.toModel(null, true)
        );
        assertEquals("User и его ID не могут быть null", exception.getMessage());
    }

    @Test
    void testToModel_NullUserId_ShouldThrowIllegalArgumentException() {
        UserResponse userWithNullId = new UserResponse();
        userWithNullId.setId(null);
        userWithNullId.setName("Test");
        userWithNullId.setEmail("test@example.com");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userLinkBuilder.toModel(userWithNullId, true)
        );
        assertEquals("User и его ID не могут быть null", exception.getMessage());
    }

    @Test
    void testToCollectionModel_ShouldReturnEntityModelWithSelfLink() {
        EntityModel<UserResponse> model = userLinkBuilder.toCollectionModel(testUser);

        assertNotNull(model);
        assertNotNull(model.getContent());
        assertEquals(testUser, model.getContent());

        Links links = model.getLinks();
        assertNotNull(links);

        assertTrue(links.hasLink("self"));
        assertEquals(1, links.toList().size());
    }

    @Test
    void testToCollectionModel_NullUser_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userLinkBuilder.toCollectionModel(null)
        );
        assertEquals("User и его ID не могут быть null", exception.getMessage());
    }

    @Test
    void testToCollectionModel_NullUserId_ShouldThrowIllegalArgumentException() {
        UserResponse userWithNullId = new UserResponse();
        userWithNullId.setId(null);
        userWithNullId.setName("Test");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userLinkBuilder.toCollectionModel(userWithNullId)
        );
        assertEquals("User и его ID не могут быть null", exception.getMessage());
    }

    @Test
    void testGetUserSelfLink_ShouldReturnCorrectSelfLink() {
        Link selfLink = userLinkBuilder.getUserSelfLink(1L);

        assertNotNull(selfLink);
        assertEquals("self", selfLink.getRel().value());
    }

    @Test
    void testGetAllUsersLink_ShouldReturnCorrectAllUsersLink() {
        Link allUsersLink = userLinkBuilder.getAllUsersLink();

        assertNotNull(allUsersLink);
        assertEquals("allUsers", allUsersLink.getRel().value());
    }

    @Test
    void testGetUpdateLink_ShouldReturnCorrectUpdateLink() {
        Link updateLink = userLinkBuilder.getUpdateLink(1L);

        assertNotNull(updateLink);
        assertEquals("update-user", updateLink.getRel().value());
    }

    @Test
    void testGetDeleteLink_ShouldReturnCorrectDeleteLink() {
        Link deleteLink = userLinkBuilder.getDeleteLink(1L);

        assertNotNull(deleteLink);
        assertEquals("delete-user", deleteLink.getRel().value());
    }

    @Test
    void testGetCreateLink_ShouldReturnCorrectCreateLink() {
        Link createLink = userLinkBuilder.getCreateLink();

        assertNotNull(createLink);
        assertEquals("create-user", createLink.getRel().value());
    }

    @Test
    void testToUserResponseWithLinks_WithActionLinks_ShouldReturnFullResponse() {
        UserResponseWithLinks response = userLinkBuilder.toUserResponseWithLinks(testUser, true);

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getAge(), response.getAge());
        assertEquals(testUser.getCreatedAt(), response.getCreatedAt());

        UserLinks links = response.getLinks();
        assertNotNull(links);

        assertEquals("/api/users/1", links.getSelf());
        assertEquals("/api/users", links.getAllUsers());
        assertEquals("/api/users", links.getCreate());
        assertEquals("/api/users/1", links.getUpdate());
        assertEquals("/api/users/1", links.getDelete());
    }

    @Test
    void testToUserResponseWithLinks_WithoutActionLinks_ShouldReturnResponseWithoutActionLinks() {
        UserResponseWithLinks response = userLinkBuilder.toUserResponseWithLinks(testUser, false);

        assertNotNull(response);

        UserLinks links = response.getLinks();
        assertNotNull(links);

        assertEquals("/api/users/1", links.getSelf());
        assertEquals("/api/users", links.getAllUsers());
        assertEquals("/api/users", links.getCreate());

        assertNull(links.getUpdate());
        assertNull(links.getDelete());
    }

    @Test
    void testBuildUserLinks_WithActionLinks_ShouldReturnAllLinks() {
        UserLinks links = userLinkBuilder.buildUserLinks(testUser, true);

        assertNotNull(links);
        assertEquals("/api/users/1", links.getSelf());
        assertEquals("/api/users", links.getAllUsers());
        assertEquals("/api/users", links.getCreate());
        assertEquals("/api/users/1", links.getUpdate());
        assertEquals("/api/users/1", links.getDelete());
    }

    @Test
    void testBuildUserLinks_WithoutActionLinks_ShouldReturnBasicLinks() {
        UserLinks links = userLinkBuilder.buildUserLinks(testUser, false);

        assertNotNull(links);
        assertEquals("/api/users/1", links.getSelf());
        assertEquals("/api/users", links.getAllUsers());
        assertEquals("/api/users", links.getCreate());
        assertNull(links.getUpdate());
        assertNull(links.getDelete());
    }

    @Test
    void testToUserResponseWithLinks_NullUser_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userLinkBuilder.toUserResponseWithLinks(null, true)
        );

        assertEquals("User и его ID не могут быть null", exception.getMessage());
    }

    @Test
    void testBuildUserLinks_NullUser_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userLinkBuilder.buildUserLinks(null, true)
        );

        assertEquals("User и его ID не могут быть null", exception.getMessage());
    }

    @Test
    void testToModel_WithMultipleUsers_ShouldGenerateCorrectLinksForEach() {
        List<UserResponse> users = Arrays.asList(testUser, testUser2);

        for (UserResponse user : users) {
            EntityModel<UserResponse> model = userLinkBuilder.toModel(user, true);
            assertNotNull(model);

            Links links = model.getLinks();
            assertTrue(links.hasLink("self"));
        }
    }

    @Test
    void testLinkGenerationConsistency_ShouldGenerateSameLinksForSameUser() {
        EntityModel<UserResponse> model1 = userLinkBuilder.toModel(testUser, true);
        EntityModel<UserResponse> model2 = userLinkBuilder.toModel(testUser, true);

        assertNotNull(model1);
        assertNotNull(model2);

        //Проверяем что оба имеют одинаковые ссылки.
        assertEquals(model1.getLinks().toList().size(), model2.getLinks().toList().size());
    }

    @Test
    void testToModel_WithEmptyUserButValidId_ShouldGenerateLinks() {
        UserResponse emptyUser = new UserResponse();
        emptyUser.setId(999L);
        emptyUser.setName("");
        emptyUser.setEmail("");
        emptyUser.setAge(null);
        emptyUser.setCreatedAt(null);

        EntityModel<UserResponse> model = userLinkBuilder.toModel(emptyUser, false);

        assertNotNull(model);
        assertEquals(emptyUser, model.getContent());

        Links links = model.getLinks();
        assertTrue(links.hasLink("self"));
        assertTrue(links.hasLink("allUsers"));
    }
}