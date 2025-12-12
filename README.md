****Spring Boot приложение для управления пользователями с использованием Spring Data JPA и PostgreSQL.****

Приложение представляет собой полноценное Spring-приложение, реализующее REST API для выполнения базовых операций CRUD (Create, Read, Update, Delete) над сущностью User.

**Особенности:**

- Spring Boot Auto-Configuration - автоматическая настройка компонентов

- Spring Data JPA Repository

- Spring REST Controllers с аннотациями @RestController, @RequestMapping

- Spring Dependency Injection через @Autowired и конструкторы

- Spring Validation с аннотациями @Valid, @Email, @Min, @Max, @NotBlank

- Spring Transaction Management через @Transactional

- Spring Exception Handling через @RestControllerAdvice

- Spring Test Framework для тестирования

**Основные технологии:**

- Java 17 - язык программирования

- Spring Boot 3.2.0 - основной фреймворк

- Spring Web - для создания REST API

- Spring Data JPA - для работы с базой данных

- Spring Validation - для валидации входных данных

- Spring Transactions - для управления транзакциями

- PostgreSQL - система управления базами данных

- Lombok - для сокращения шаблонного кода

- Testcontainers - для интеграционного тестирования с PostgreSQL

- Mockito - для модульного тестирования

- Maven - для управления зависимостями и сборки

****Сущность User****

**Поля сущности User:**

- id (Long) - уникальный идентификатор

- name (String) - имя пользователя (обязательное)

- email (String) - email пользователя (обязательное, уникальное)

- age (Integer) - возраст пользователя (0-120, необязательное)

- createdAt (Instant) - дата и время создания записи

****Тестирование****

**Проект включает два типа тестов:**

1. Интеграционные тесты (UserControllerTest)
   
   - Используют Testcontainers для запуска PostgreSQL в Docker

   - Тестируют весь стек приложения через MockMvc

   - Каждый тест выполняется в транзакции с автоматическим откатом

2. Модульные тесты (UserServiceTest, GlobalExceptionHandlerTest)

   - Используют Mockito для изоляции тестируемых компонентов

   - Тестируют бизнес-логику и обработку исключений