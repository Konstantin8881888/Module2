package org.klimtsov;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppIntegrationTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void appStartsAndShowsMenu() {
        String input = "6\n"; // Выбираем выход
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        App.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("=== Пользовательский сервис ==="));
        assertTrue(output.contains("1) Создать пользователя"));
        assertTrue(output.contains("6) Выход"));
    }

    @Test
    public void appHandlesInvalidMenuOption() {
        String input = "invalid\n6\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        App.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("Нет такой опции"));
    }
}