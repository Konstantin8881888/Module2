package org.klimtsov.console;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleHelperTest {
    private ConsoleHelper consoleHelper;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outputStream);
        System.setOut(ps);
        System.setErr(ps);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);
    }

    private void provideInput(String data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
        consoleHelper = new ConsoleHelper(); //Пересоздаем для нового input stream.
    }

    @Test
    public void readLine_WithPrompt_DisplaysPromptAndReturnsInput() {
        provideInput("test input\n");

        String result = consoleHelper.readLine("Введите текст: ");

        assertEquals("test input", result);
        assertTrue(getOutput().contains("Введите текст:"));
    }

    @Test
    public void readInt_ValidInput_ReturnsInteger() {
        provideInput("42\n");

        int result = consoleHelper.readInt("Введите число:", 0);

        assertEquals(42, result);
    }

    @Test
    public void readInt_EmptyInput_ReturnsDefault() {
        provideInput("\n");

        int result = consoleHelper.readInt("Введите число:", 99);

        assertEquals(99, result);
    }

    @Test
    public void readInt_InvalidThenValidInput_ReturnsInteger() {
        provideInput("invalid\n42\n");

        int result = consoleHelper.readInt("Введите число:", 0);

        assertEquals(42, result);
        assertTrue(getOutput().contains("Нужно ввести целое число"));
    }

    @Test
    public void readLong_ValidInput_ReturnsLong() {
        provideInput("123456789\n");

        Long result = consoleHelper.readLong("Введите long:");

        assertEquals(123456789L, result);
    }

    @Test
    public void readLong_InvalidThenValidInput_ReturnsLong() {
        provideInput("not a number\n123\n");

        Long result = consoleHelper.readLong("Введите long:");

        assertEquals(123L, result);
        assertTrue(getOutput().contains("Нужно ввести число"));
    }

    @Test
    public void readNonEmpty_ValidInput_ReturnsString() {
        provideInput("non empty\n");

        String result = consoleHelper.readNonEmpty("Введите текст:");

        assertEquals("non empty", result);
    }

    @Test
    public void readNonEmpty_EmptyThenValidInput_ReturnsString() {
        provideInput("\nvalid\n");

        String result = consoleHelper.readNonEmpty("Введите текст:");

        assertEquals("valid", result);
        assertTrue(getOutput().contains("Значение не должно быть пустым"));
    }

    @Test
    public void readNonEmpty_MultipleEmptyThenValidInput_ReturnsString() {
        provideInput("\n\n\nfinally valid\n");

        String result = consoleHelper.readNonEmpty("Введите текст:");

        assertEquals("finally valid", result);
        //Проверяем что сообщение об ошибке выводилось несколько раз.
        String output = getOutput();
        int errorCount = countOccurrences(output);
        assertTrue(errorCount >= 2);
    }

    // Email tests
    @Test
    public void readEmail_ValidInput_ReturnsEmail() {
        provideInput("user@example.com\n");
        String result = consoleHelper.readEmail("Email: ");
        assertEquals("user@example.com", result);
    }

    @Test
    public void readEmail_InvalidThenValid_ReturnsEmail() {
        provideInput("invalid-email\nuser@site.com\n");
        String result = consoleHelper.readEmail("Email: ");
        assertEquals("user@site.com", result);
        assertTrue(getOutput().contains("Неверный формат email"));
    }

    // readAge tests
    @Test
    public void readAge_ValidInput_ReturnsAge() {
        provideInput("25\n");
        int result = consoleHelper.readAge("Возраст");
        assertEquals(25, result);
    }

    @Test
    public void readAge_InvalidThenValidInput_ReturnsAgeAndPrintsErrors() {
        provideInput("abc\n-1\n121\n30\n");
        int result = consoleHelper.readAge("Возраст");
        assertEquals(30, result);
        String out = getOutput();
        assertTrue(out.contains("Нужно ввести целое число") || out.contains("Возраст должен быть от 0 до 120"));
        assertTrue(out.contains("Возраст должен быть от 0 до 120"));
    }

    // readOptionalAge tests
    @Test
    public void readOptionalAge_EnterReturnsNull() {
        provideInput("\n");
        Integer result = consoleHelper.readOptionalAge("Возраст");
        assertNull(result);
    }

    @Test
    public void readOptionalAge_InvalidThenValid_ReturnsIntegerAndShowsMessage() {
        provideInput("999\n50\n");
        Integer result = consoleHelper.readOptionalAge("Возраст");
        assertNotNull(result);
        assertEquals(50, result.intValue());
        assertTrue(getOutput().contains("Возраст должен быть от 0 до 120"));
    }

    // readOptionalEmail tests
    @Test
    public void readOptionalEmail_EnterReturnsNull() {
        provideInput("\n");
        String result = consoleHelper.readOptionalEmail("Email");
        assertNull(result);
    }

    @Test
    public void readOptionalEmail_InvalidThenValid_ReturnsEmailAndShowsMessage() {
        provideInput("bad-email\nuser@domain.com\n");
        String result = consoleHelper.readOptionalEmail("Email");
        assertEquals("user@domain.com", result);
        assertTrue(getOutput().contains("Неверный формат email"));
    }

    //вспомогательные методы.
    private String getOutput() {
        return outputStream.toString();
    }

    private int countOccurrences(String text) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf("Значение не должно быть пустым", index)) != -1) {
            count++;
            index += "Значение не должно быть пустым".length();
        }
        return count;
    }
}
