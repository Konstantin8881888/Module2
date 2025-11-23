package org.klimtsov.console;

import java.util.Scanner;
import java.util.regex.Pattern;

public class ConsoleHelper {
    private final Scanner scanner = new Scanner(System.in);

    //Простой regex для проверки email.
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");

    public String readLine(String prompt) {
        //Печатаем подсказку так, чтобы подсказки и логи были в одном потоке.
        System.err.print(prompt);
        System.err.flush();
        return scanner.nextLine().trim();
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) return s;
            System.err.println("Значение не должно быть пустым. Попробуйте ещё раз.");
        }
    }

    //Возвращает целое число; если введена пустая строка — возвращает defaultValue.
    public int readInt(String prompt, int defaultValue) {
        while (true) {
            String s = readLine(prompt + " (int, пусто для " + defaultValue + "): ");
            if (s.isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                System.err.println("Нужно ввести целое число. Попробуйте ещё раз.");
            }
        }
    }

    //Проверяем, что возраст целое в диапазоне от 0 до 120.
    public int readAge(String prompt) {
        while (true) {
            String s = readLine(prompt + " (целое 0..120): ");
            try {
                int v = Integer.parseInt(s.trim());
                if (v < 0 || v > 120) {
                    System.err.println("Возраст должен быть от 0 до 120. Попробуйте ещё раз.");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.err.println("Нужно ввести целое число. Попробуйте ещё раз.");
            }
        }
    }

    //Для обновления: Enter - оставить старое, иначе проверяем диапазон.
    public Integer readOptionalAge(String prompt) {
        while (true) {
            String s = readLine(prompt + ": ");
            if (s.isBlank()) return null;
            try {
                int v = Integer.parseInt(s.trim());
                if (v < 0 || v > 120) {
                    System.err.println("Возраст должен быть от 0 до 120. Попробуйте ещё раз.");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.err.println("Нужно ввести целое число или нажать Enter. Попробуйте ещё раз.");
            }
        }
    }

    // Проверяем email на правильный вид.
    public String readEmail(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) {
                System.err.println("Email не может быть пустым. Попробуйте ещё раз.");
                continue;
            }
            if (!EMAIL_PATTERN.matcher(s).matches()) {
                System.err.println("Неверный формат email. Пример корректного: user@example.com");
                continue;
            }
            return s;
        }
    }

    //Для обновления: Enter - оставить старое, иначе проверяем формат.
    public String readOptionalEmail(String prompt) {
        while (true) {
            String s = readLine(prompt + ": ");
            if (s.isBlank()) return null;
            if (!EMAIL_PATTERN.matcher(s).matches()) {
                System.err.println("Неверный формат email. Пример корректного: user@example.com");
                continue;
            }
            return s;
        }
    }

    public Long readLong(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                System.err.println("Нужно ввести число. Попробуйте ещё раз.");
            }
        }
    }
}
