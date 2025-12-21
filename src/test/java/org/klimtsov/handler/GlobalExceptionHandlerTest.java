package org.klimtsov.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.klimtsov.dto.ErrorResponse;
import org.klimtsov.exception.UserNotFoundException;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptions_ReturnsBadRequestWithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/users");
        ServletWebRequest webRequest = new ServletWebRequest(servletRequest);

        FieldError emailError = new FieldError("userRequest", "email", "Некорректный формат email");
        FieldError ageError = new FieldError("userRequest", "age", "Возраст должен быть от 0 до 120");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(emailError, ageError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorResponse errorResponse = response.getBody();
        assertTrue(errorResponse.getMessage().contains("Некорректный формат email"));
        assertTrue(errorResponse.getMessage().contains("Возраст должен быть от 0 до 120"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("/api/users", errorResponse.getPath());
    }

    @Test
    void handleValidationExceptions_WithObjectError_HandlesGracefully() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/users");
        ServletWebRequest webRequest = new ServletWebRequest(servletRequest);

        org.springframework.validation.ObjectError objectError =
                new org.springframework.validation.ObjectError("user", "Общая ошибка объекта");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(objectError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleIllegalArgumentException_ReturnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Пользователь не найден");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/api/users/999");
        ServletWebRequest webRequest = new ServletWebRequest(servletRequest);

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorResponse errorResponse = response.getBody();
        assertEquals("Пользователь не найден", errorResponse.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("/api/users/999", errorResponse.getPath());
    }

    @Test
    void handleUserNotFoundException_ReturnsNotFound() {
        UserNotFoundException ex = new UserNotFoundException(999L);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/api/users/999");
        ServletWebRequest webRequest = new ServletWebRequest(servletRequest);

        ResponseEntity<ErrorResponse> response = handler.handleUserNotFoundException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorResponse errorResponse = response.getBody();
        assertEquals("Пользователь с ID 999 не найден", errorResponse.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getError());
        assertEquals("/api/users/999", errorResponse.getPath());
    }

    @Test
    void handleGeneralException_ReturnsInternalServerError() {
        Exception ex = new RuntimeException("Неизвестная ошибка");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/users");
        ServletWebRequest webRequest = new ServletWebRequest(servletRequest);

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorResponse errorResponse = response.getBody();
        assertTrue(errorResponse.getMessage().contains("Неизвестная ошибка"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
    }

    @Test
    void handleGeneralException_WithNullMessage_ReturnsInternalServerError() {
        Exception ex = new RuntimeException();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/users");
        ServletWebRequest webRequest = new ServletWebRequest(servletRequest);

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorResponse errorResponse = response.getBody();
        assertEquals("Внутренняя ошибка сервера: null", errorResponse.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
    }

    @Test
    void handleMethodArgumentTypeMismatchException_ReturnsBadRequest() {
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex =
                mock(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/api/users/abc");
        ServletWebRequest webRequest = new ServletWebRequest(servletRequest);

        when(ex.getName()).thenReturn("id");
        when(ex.getRequiredType()).thenReturn((Class) Long.class);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        ErrorResponse errorResponse = response.getBody();
        assertTrue(errorResponse.getMessage().contains("Параметр 'id' должен быть числом"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("/api/users/abc", errorResponse.getPath());
    }
}