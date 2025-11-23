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
            String choice = ch.readLine("\nВыберите пункт: ");

            try {
                switch (choice) {
                    case "1" -> {
                        String name = ch.readNonEmpty("Имя: ");
                        String email = ch.readEmail("Email: ");
                        int age = ch.readAge("Возраст: ");
                        User user = new User(null, name, email, age, Instant.now());
                        Long id = dao.create(user);
                        System.out.println("Created user id=" + id);
                    }
                    case "2" -> {
                        List<User> all = dao.findAll();
                        all.forEach(System.out::println);
                    }
                    case "3" -> {
                        Long id = ch.readLong("Введите id: ");
                        Optional<User> u = dao.findById(id);
                        System.out.println(u.map(Object::toString).orElse("Пользователь с таким id не найден."));
                    }
                    case "4" -> {
                        Long id = ch.readLong("Введите id пользователя, данные которого нужно изменить, Enter - чтобы оставить значение поля старым: ");
                        Optional<User> maybe = dao.findById(id);
                        if (maybe.isEmpty()) {
                            System.out.println("User not found");
                            break;
                        }
                        User u = maybe.get();

                        // Выводим текущие данные в stderr (чтобы не перепуталось с подсказкой)
                        System.err.println("Текущие данные: " + u);

                        String newName = ch.readLine("Новое имя: ");
                        if (!newName.isBlank()) u.setName(newName);

                        String newEmail = ch.readOptionalEmail("Новый email: ");
                        if (newEmail != null) u.setEmail(newEmail);

                        Integer newAge = ch.readOptionalAge("Новый возраст: ");
                        if (newAge != null) u.setAge(newAge);

                        dao.update(u);
                        System.out.println("Данные обновлены.");
                    }

                    case "5" -> {
                        Long idToDelete = ch.readLong("Введите id пользователя, которого нужно удалить: ");
                        boolean wasDeleted = dao.delete(idToDelete);
                        if (wasDeleted) {
                            System.out.println("Пользователь с id=" + idToDelete + " был удалён.");
                        } else {
                            System.out.println("Пользователь с id=" + idToDelete + " не был найден.");
                        }
                    }
                    case "6" -> {
                        break mainLoop;
                    }
                    default -> System.out.println("Нет такой опции.");
                }
            } catch (Exception e) {
                logger.error("Error in operation", e);
                System.out.println("Произошла ошибка: " + e.getMessage());
            }
        }

        HibernateUtil.shutdown();
        System.out.println("Сервис завершён.");
    }
}