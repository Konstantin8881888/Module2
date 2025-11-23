package org.klimtsov.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.regex.Pattern;

public class ConsoleHelper {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleHelper.class);
    private final Scanner scanner = new Scanner(System.in);

    //Простой regex для проверки email.
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");

    public String readLine(String prompt) {
        logger.debug("Запрос ввода: prompt='{}'", prompt);
        System.out.print(prompt);
        System.out.flush();
        String input = scanner.nextLine().trim();
        logger.debug("Пользовательский ввод: '{}'", input);
        return input;
    }

    public String readNonEmpty(String prompt) {
        logger.debug("Запрос непустого значения: prompt='{}'", prompt);
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) {
                logger.debug("Получено непустое значение: '{}'", s);
                return s;
            }
            logger.warn("Пользователь ввел пустое значение для обязательного поля");
            System.out.println("Значение не должно быть пустым. Попробуйте ещё раз.");
        }
    }

    //Возвращает целое число; если введена пустая строка — возвращает defaultValue.
    public int readInt(String prompt, int defaultValue) {
        logger.debug("Запрос целого числа: prompt='{}', defaultValue={}", prompt, defaultValue);
        while (true) {
            String s = readLine(prompt + " (int, пусто для " + defaultValue + "): ");
            if (s.isEmpty()) {
                logger.debug("Использовано значение по умолчанию: {}", defaultValue);
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(s.trim());
                logger.debug("Получено целое число: {}", value);
                return value;
            } catch (NumberFormatException e) {
                logger.warn("Некорректный формат числа: '{}'", s);
                System.out.println("Нужно ввести целое число. Попробуйте ещё раз.");
            }
        }
    }

    //Проверяем, что возраст целое в диапазоне от 0 до 120.
    public int readAge(String prompt) {
        logger.debug("Запрос возраста: prompt='{}'", prompt);
        while (true) {
            String s = readLine(prompt + " (целое 0..120): ");
            try {
                int v = Integer.parseInt(s.trim());
                if (v < 0 || v > 120) {
                    logger.warn("Некорректный возраст: {}", v);
                    System.out.println("Возраст должен быть от 0 до 120. Попробуйте ещё раз.");
                    continue;
                }
                logger.debug("Получен корректный возраст: {}", v);
                return v;
            } catch (NumberFormatException e) {
                logger.warn("Некорректный формат возраста: '{}'", s);
                System.out.println("Нужно ввести целое число. Попробуйте ещё раз.");
            }
        }
    }

    //Для обновления: Enter - оставить старое, иначе проверяем диапазон.
    public Integer readOptionalAge(String prompt) {
        logger.debug("Запрос опционального возраста: prompt='{}'", prompt);
        while (true) {
            String s = readLine(prompt + ": ");
            if (s.isBlank()) {
                logger.debug("Пользователь оставил возраст без изменений (пустой ввод)");
                return null;
            }
            try {
                int v = Integer.parseInt(s.trim());
                if (v < 0 || v > 120) {
                    logger.warn("Некорректный опциональный возраст: {}", v);
                    System.out.println("Возраст должен быть от 0 до 120. Попробуйте ещё раз.");
                    continue;
                }
                logger.debug("Получен корректный опциональный возраст: {}", v);
                return v;
            } catch (NumberFormatException e) {
                logger.warn("Некорректный формат опционального возраста: '{}'", s);
                System.out.println("Нужно ввести целое число или нажать Enter. Попробуйте ещё раз.");
            }
        }
    }

    // Проверяем email на правильный вид.
    public String readEmail(String prompt) {
        logger.debug("Запрос email: prompt='{}'", prompt);
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) {
                logger.warn("Пользователь ввел пустой email");
                System.out.println("Email не может быть пустым. Попробуйте ещё раз.");
                continue;
            }
            if (!EMAIL_PATTERN.matcher(s).matches()) {
                logger.warn("Некорректный формат email: '{}'", s);
                System.out.println("Неверный формат email. Пример корректного: user@example.com");
                continue;
            }
            logger.debug("Получен корректный email: '{}'", s);
            return s;
        }
    }

    //Для обновления: Enter - оставить старое, иначе проверяем формат.
    public String readOptionalEmail(String prompt) {
        logger.debug("Запрос опционального email: prompt='{}'", prompt);
        while (true) {
            String s = readLine(prompt + ": ");
            if (s.isBlank()) {
                logger.debug("Пользователь оставил email без изменений (пустой ввод)");
                return null;
            }
            if (!EMAIL_PATTERN.matcher(s).matches()) {
                logger.warn("Некорректный формат опционального email: '{}'", s);
                System.out.println("Неверный формат email. Пример корректного: user@example.com");
                continue;
            }
            logger.debug("Получен корректный опциональный email: '{}'", s);
            return s;
        }
    }

    public Long readLong(String prompt) {
        logger.debug("Запрос long числа: prompt='{}'", prompt);
        while (true) {
            String s = readLine(prompt);
            try {
                Long value = Long.parseLong(s.trim());
                logger.debug("Получено long число: {}", value);
                return value;
            } catch (NumberFormatException e) {
                logger.warn("Некорректный формат long числа: '{}'", s);
                System.out.println("Нужно ввести число. Попробуйте ещё раз.");
            }
        }
    }
}