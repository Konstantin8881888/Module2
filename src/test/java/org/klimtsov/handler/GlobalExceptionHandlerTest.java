//Тесты здесь не дублируют тесты UserControllerTest, поскольку там интеграционные.
package org.klimtsov.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptions_ReturnsBadRequestWithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError emailError = new FieldError("userRequest", "email", "Некорректный формат email");
        FieldError ageError = new FieldError("userRequest", "age", "Возраст должен быть от 0 до 120");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(emailError, ageError));

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Некорректный формат email", response.getBody().get("email"));
        assertEquals("Возраст должен быть от 0 до 120", response.getBody().get("age"));
        assertEquals(2, response.getBody().size());
    }

    @Test
    void handleValidationExceptions_WithObjectError_HandlesGracefully() {
        //Проверяем, что если придет не FieldError, а ObjectError.
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        //Создаем ObjectError (не FieldError).
        org.springframework.validation.ObjectError objectError =
                new org.springframework.validation.ObjectError("user", "Общая ошибка объекта");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(objectError));

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);
    }

    @Test
    void handleIllegalArgumentException_ReturnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Пользователь не найден");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Пользователь не найден", response.getBody().get("error"));
        assertEquals(1, response.getBody().size());
    }

    @Test
    void handleGeneralException_ReturnsInternalServerError() {
        Exception ex = new RuntimeException("Неизвестная ошибка");

        ResponseEntity<Map<String, String>> response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Внутренняя ошибка сервера", response.getBody().get("error"));
        assertEquals("Неизвестная ошибка", response.getBody().get("message"));
    }

    @Test
    void handleGeneralException_WithNullMessage_ReturnsInternalServerError() {
        Exception ex = new RuntimeException();

        ResponseEntity<Map<String, String>> response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Внутренняя ошибка сервера", response.getBody().get("error"));
        assertNull(response.getBody().get("message")); // message будет null
    }
}