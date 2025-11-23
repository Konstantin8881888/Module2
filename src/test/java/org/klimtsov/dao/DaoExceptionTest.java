package org.klimtsov.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DaoExceptionTest {

    @Test
    public void daoException_ConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("Exception cause");
        DaoException exception = new DaoException("Test message", cause);

        assertEquals("Test message", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void daoException_MessageIncludesCauseMessage() {
        Throwable cause = new RuntimeException("Exception cause");
        DaoException exception = new DaoException("Test message", cause);

        assertTrue(exception.getMessage().contains("Test message"));
        assertTrue(exception.getCause().getMessage().contains("Exception cause"));
    }
}