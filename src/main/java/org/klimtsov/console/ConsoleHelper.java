package org.klimtsov.console;

import java.util.Scanner;

public class ConsoleHelper {
    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt, int defaultValue) {
        while (true) {
            String s = readLine(prompt + ": ");
            if (s.isEmpty()) return defaultValue;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести целое число. Попробуйте ещё раз.");
            }
        }
    }

    public Long readLong(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести число. Попробуйте ещё раз.");
            }
        }
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) return s;
            System.out.println("Значение не должно быть пустым.");
        }
    }
}
