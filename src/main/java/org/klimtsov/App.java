package org.klimtsov;

import org.klimtsov.console.ConsoleHelper;
import org.klimtsov.dao.UserDao;
import org.klimtsov.dao.UserDaoImpl;
import org.klimtsov.userservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Запуск пользовательского сервиса");
        ConsoleHelper ch = new ConsoleHelper();
        UserDao dao = new UserDaoImpl();

        mainLoop:
        while (true) {
            System.out.println("\n=== Пользовательский сервис ===");
            System.out.println("1) Создать пользователя.");
            System.out.println("2) Список всех пользователей.");
            System.out.println("3) Поиск пользователя по id.");
            System.out.println("4) Обновить пользователя.");
            System.out.println("5) Удалить пользователя.");
            System.out.println("6) Выход.");
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            String choice = ch.readLine("\nВыберите пункт: ");
            logger.debug("Пользователь выбрал пункт меню: {}", choice);

            try {
                switch (choice) {
                    case "1" -> {
                        logger.info("Начало создания пользователя");
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                        String name = ch.readNonEmpty("Имя: ");
                        String email = ch.readEmail("Email: ");
                        int age = ch.readAge("Возраст: ");
                        logger.debug("Введены данные: name={}, email={}, age={}", name, email, age);
                        User user = new User(null, name, email, age, Instant.now());
                        Long id = dao.create(user);
                        logger.info("Пользователь создан успешно: id={}", id);
                        System.out.println("Created user id=" + id);
                    }
                    case "2" -> {
                        logger.info("Запрос списка всех пользователей");
                        List<User> all = dao.findAll();
                        logger.info("Найдено пользователей: {}", all.size());
                        logger.debug("Список пользователей: {}", all);
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                        all.forEach(System.out::println);
                    }
                    case "3" -> {
                        Long id = ch.readLong("Введите id: ");
                        logger.info("Поиск пользователя по id: {}", id);
                        Optional<User> u = dao.findById(id);
                        if (u.isPresent()) {
                            logger.info("Пользователь найден: id={}, email={}", id, u.get().getEmail());
                            System.out.println(u.get());
                        } else {
                            logger.warn("Пользователь не найден: id={}", id);
                            System.out.println("Пользователь с таким id не найден.");
                        }
                    }
                    case "4" -> {
                        Long id = ch.readLong("Введите id пользователя, данные которого нужно изменить: ");
                        logger.info("Начало обновления пользователя: id={}", id);
                        Optional<User> maybe = dao.findById(id);
                        if (maybe.isEmpty()) {
                            logger.warn("Пользователь для обновления не найден: id={}", id);
                            System.out.println("User not found");
                            break;
                        }
                        User u = maybe.get();
                        logger.debug("Текущие данные пользователя: {}", u);

                        System.err.println("Текущие данные: " + u);

                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                        String newName = ch.readLine("Новое имя: ");
                        if (!newName.isBlank()) {
                            logger.debug("Изменение имени с '{}' на '{}'", u.getName(), newName);
                            u.setName(newName);
                        }

                        String newEmail = ch.readOptionalEmail("Новый email: ");
                        if (newEmail != null) {
                            logger.debug("Изменение email с '{}' на '{}'", u.getEmail(), newEmail);
                            u.setEmail(newEmail);
                        }

                        Integer newAge = ch.readOptionalAge("Новый возраст: ");
                        if (newAge != null) {
                            logger.debug("Изменение возраста с {} на {}", u.getAge(), newAge);
                            u.setAge(newAge);
                        }

                        dao.update(u);
                        logger.info("Пользователь обновлен успешно: id={}", id);
                        System.out.println("Данные обновлены.");
                    }
                    case "5" -> {
                        Long idToDelete = ch.readLong("Введите id пользователя, которого нужно удалить: ");
                        logger.info("Попытка удаления пользователя: id={}", idToDelete);
                        boolean wasDeleted = dao.delete(idToDelete);
                        if (wasDeleted) {
                            logger.info("Пользователь удален успешно: id={}", idToDelete);
                            System.out.println("Пользователь с id=" + idToDelete + " был удалён.");
                        } else {
                            logger.warn("Пользователь для удаления не найден: id={}", idToDelete);
                            System.out.println("Пользователь с id=" + idToDelete + " не был найден.");
                        }
                    }
                    case "6" -> {
                        logger.info("Завершение работы пользовательского сервиса");
                        break mainLoop;
                    }
                    default -> {
                        logger.warn("Выбрана несуществующая опция меню: {}", choice);
                        System.out.println("Нет такой опции.");
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при выполнении операции '{}': {}", choice, e.getMessage(), e);
                System.out.println("Произошла ошибка: " + e.getMessage());
            }
        }

        logger.info("Завершение работы приложения");
        HibernateUtil.shutdown();
        System.out.println("Сервис завершён.");
    }
}