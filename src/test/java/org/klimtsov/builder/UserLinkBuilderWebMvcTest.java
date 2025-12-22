package org.klimtsov.builder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.klimtsov.controller.UserController;
import org.klimtsov.dto.*;
import org.klimtsov.service.UserService;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserLinkBuilderWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserLinkBuilder userLinkBuilder;

    @Test
    void testGeneratedLinksAreAccessible() throws Exception {
        // Arrange
        UserResponse mockUser = new UserResponse();
        mockUser.setId(1L);
        mockUser.setName("Mock User");
        mockUser.setEmail("mock@test.com");
        mockUser.setAge(25);
        mockUser.setCreatedAt(Instant.now());

        when(userService.getUserById(1L)).thenReturn(mockUser);

        UserResponseWithLinks mockResponse = new UserResponseWithLinks();
        mockResponse.setId(1L);
        mockResponse.setName("Mock User");
        mockResponse.setEmail("mock@test.com");
        mockResponse.setAge(25);
        mockResponse.setCreatedAt(Instant.now());

        UserLinks links = new UserLinks();
        links.setSelf("/api/users/1");
        links.setAllUsers("/api/users");
        links.setCreate("/api/users");
        links.setUpdate("/api/users/1");
        links.setDelete("/api/users/1");
        mockResponse.setLinks(links);

        when(userLinkBuilder.toUserResponseWithLinks(any(UserResponse.class), anyBoolean()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mock User"))
                .andExpect(jsonPath("$._links.self").value("/api/users/1"));
    }

    @Test
    void testLinkBuilderIntegrationWithController() throws Exception {
        // Arrange
        UserResponse mockUser = new UserResponse();
        mockUser.setId(99L);
        mockUser.setName("Integration Test");
        mockUser.setEmail("integration@test.com");
        mockUser.setAge(30);
        mockUser.setCreatedAt(Instant.now());

        UserResponseWithLinks mockResponseWithLinks = new UserResponseWithLinks();
        mockResponseWithLinks.setId(99L);
        mockResponseWithLinks.setName("Integration Test");
        mockResponseWithLinks.setEmail("integration@test.com");
        mockResponseWithLinks.setAge(30);
        mockResponseWithLinks.setCreatedAt(Instant.now());

        UserLinks userLinks = new UserLinks();
        userLinks.setSelf("/api/users/99");
        userLinks.setAllUsers("/api/users");
        userLinks.setCreate("/api/users");
        userLinks.setUpdate("/api/users/99");
        userLinks.setDelete("/api/users/99");
        mockResponseWithLinks.setLinks(userLinks);

        when(userService.getUserById(99L)).thenReturn(mockUser);
        when(userLinkBuilder.toUserResponseWithLinks(mockUser, true)).thenReturn(mockResponseWithLinks);

        // Act & Assert
        mockMvc.perform(get("/api/users/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.name").value("Integration Test"))
                .andExpect(jsonPath("$._links.self").value("/api/users/99"))
                .andExpect(jsonPath("$._links.allUsers").value("/api/users"))
                .andExpect(jsonPath("$._links.create").value("/api/users"))
                .andExpect(jsonPath("$._links.update").value("/api/users/99"))
                .andExpect(jsonPath("$._links.delete").value("/api/users/99"));
    }
}