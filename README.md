### ****Spring Boot приложение для управления пользователями с использованием Spring Data JPA и PostgreSQL.****

Приложение представляет собой полноценное Spring-приложение, реализующее REST API для выполнения базовых операций CRUD (Create, Read, Update, Delete) над сущностью User.

### Особенности:

- Spring Boot Auto-Configuration - автоматическая настройка компонентов

- Spring Data JPA Repository

- Spring REST Controllers с аннотациями @RestController, @RequestMapping

- Spring Dependency Injection через @Autowired и конструкторы

- Spring Validation с аннотациями @Valid, @Email, @Min, @Max, @NotBlank

- Spring Transaction Management через @Transactional

- Spring Exception Handling через @RestControllerAdvice

- Spring Test Framework для тестирования

### Основные технологии:

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

---

## Сущность User

### Поля сущности User:

- id (Long) - уникальный идентификатор

- name (String) - имя пользователя (обязательное)

- email (String) - email пользователя (обязательное, уникальное)

- age (Integer) - возраст пользователя (0-120, необязательное)

- createdAt (Instant) - дата и время создания записи

---

## Тестирование

### Проект включает два типа тестов:

1. Интеграционные тесты (UserControllerTest)
   
   - Используют Testcontainers для запуска PostgreSQL в Docker

   - Тестируют весь стек приложения через MockMvc

   - Каждый тест выполняется в транзакции с автоматическим откатом

2. Модульные тесты (UserServiceTest, GlobalExceptionHandlerTest)

   - Используют Mockito для изоляции тестируемых компонентов

   - Тестируют бизнес-логику и обработку исключений

---

## REST API Endpoints

| Метод | Путь | Описание |
| --- | --- | --- |
| `POST` | `/api/users` | Создание нового пользователя |
| `GET`  | `/api/users` | Получение списка всех пользователей |
| `GET`  | `/api/users/{id}` | Получение пользователя по ID |
| `PUT`  | `/api/users/{id}` | Обновление данных пользователя |
| `DELETE` | `/api/users/{id}` | Удаление пользователя |

---

## Примеры запросов

### 1. Создание пользователя (POST)

**Запрос:**

POST /api/users

Host: localhost:8080

Content-Type: application/json

    {
        "name": "Иван Иванов",
        "email": "ivan@example.com",
        "age": 25
    }

**Пример ответа (201 Created):**

    {
    "id": 1,
    "name": "Иван Иванов",
    "email": "ivan@example.com",
    "age": 25,
    "createdAt": "2024-01-15T10:30:00.000Z"
    }

---

### 2. Получение всех пользователей (GET)

**Запрос:**

GET /api/users

Host: localhost:8080

Accept: application/json

**Пример ответа (200 OK):**

    [
    {
    "id": 1,
    "name": "Иван Иванов",
    "email": "ivan@example.com",
    "age": 25,
    "createdAt": "2024-01-15T10:30:00.000Z"
    },
    {
    "id": 2,
    "name": "Мария Петрова",
    "email": "maria@example.com",
    "age": 30,
    "createdAt": "2024-01-16T14:20:00.000Z"
    }
    ]

---

### 3. Получение пользователя по ID (GET)

**Запрос:**

GET /api/users/1

Host: localhost:8080

Accept: application/json

**Пример ответа (200 OK):**

    {
    "id": 1,
    "name": "Иван Иванов",
    "email": "ivan@example.com",
    "age": 25,
    "createdAt": "2024-01-15T10:30:00.000Z"
    }

---

### 4. Обновление пользователя (PUT)

**Запрос:**

PUT /api/users/1

Host: localhost:8080

Content-Type: application/json

    {
        "name": "Иван Петров",
        "email": "ivan.p@example.com",
        "age": 26
    }

**Пример ответа (200 OK):**

    {
    "id": 1,
    "name": "Иван Петров",
    "email": "ivan.p@example.com",
    "age": 26,
    "createdAt": "2024-01-15T10:30:00.000Z"
    }

---

### 5. Удаление пользователя (DELETE)

**Запрос:**

DELETE /api/users/1

Host: localhost:8080

**Пример ответа (204 No Content):**

(тело ответа отсутствует)

---

### 6. Ошибка валидации (400 Bad Request)

**Некорректный запрос:**

POST /api/users

Host: localhost:8080

Content-Type: application/json

    {
        "name": "",
        "email": "invalid-email",
        "age": 150
    }

**Пример ответа (400 Bad Request):**

    {
    "name": "Имя не может быть пустым",
    "email": "Некорректный формат email",
    "age": "Возраст должен быть до 120"
    }

---

### 7. Пользователь не найден (400 Bad Request)


**Запрос:**

GET /api/users/999

Host: localhost:8080

Accept: application/json

**Пример ответа (400 Bad Request):**

    {
    "error": "Пользователь не найден с id: 999"
    }

---
