### Spring Boot приложение для управления пользователями с использованием Spring Data JPA, PostgreSQL, Kafka и HATEOAS.

Приложение представляет собой полноценный микросервис (user-service), реализующий REST API для выполнения базовых операций CRUD (Create, Read, Update, Delete) над сущностью User с полной поддержкой HATEOAS и отправляющий события о действиях в Kafka.

### Архитектура Spring Cloud

Сервис интегрирован в Spring Cloud со следующими особенностями:

- **Service Discovery**: Регистрация в Eureka Server (порт 8761)
- **External Configuration**: Получение настроек из Config Server (порт 8888)
- **API Gateway**: Доступ через единую точку входа (порт 8080)
- **Circuit Breaker**: Защита через Resilience4j в Gateway

**Порты:**
- Сервис напрямую: `http://localhost:8081`
- Через Gateway: `http://localhost:8080/api/users/*`

### Особенности:

- Spring Boot Auto-Configuration - автоматическая настройка компонентов
- Spring Data JPA Repository
- Spring REST Controllers с аннотациями @RestController, @RequestMapping
- Spring Dependency Injection через @Autowired и конструкторы
- Spring Validation с аннотациями @Valid, @Email, @Min, @Max, @NotBlank
- Spring Transaction Management через @Transactional
- Spring Exception Handling через @RestControllerAdvice
- Spring Test Framework для тестирования
- Apache Kafka - для отправки событий о создании и удалении пользователей
- Spring HATEOAS - для реализации принципов REST Level 3 с гипермедиа ссылками
- SpringDoc OpenAPI - для автоматической генерации документации API
- HAL - формат ответов с встроенными ссылками
- Паттерн DTO - для отделения API модели от сущности БД

### Основные технологии:

- Java 17 - язык программирования
- Spring Boot 3.5.8 - основной фреймворк
- Spring Web - для создания REST API
- Spring Data JPA - для работы с базой данных
- Spring Validation - для валидации входных данных
- Spring Transactions - для управления транзакциями
- Spring for Apache Kafka - для асинхронной отправки событий
- PostgreSQL - система управления базами данных
- Lombok - для сокращения шаблонного кода
- Testcontainers - для интеграционного тестирования с PostgreSQL
- Mockito - для модульного тестирования
- Maven - для управления зависимостями и сборки
- Spring HATEOAS - для гипермедиа в REST API
- springdoc-openapi - для документации API
- Spring Cloud Config Client - для получения конфигурации из центрального сервера
- Spring Cloud Netflix Eureka Client - для регистрации в сервисе обнаружения

---

## Сущность User

### Поля сущности User:

- id (Long) - уникальный идентификатор
- name (String) - имя пользователя (обязательное)
- email (String) - email пользователя (обязательное, уникальное)
- age (Integer) - возраст пользователя (0-120, необязательное)
- createdAt (Instant) - дата и время создания записи

---

## Архитектура и интеграция

Приложение отправляет события в Apache Kafka при создании или удалении пользователя. Эти события потребляются вторым микросервисом (notification-service) для отправки email-уведомлений.

### Особенности HATEOAS:
- Все ответы API содержат гиперссылки на связанные ресурсы
- Динамическое обнаружение доступных действий
- Ссылки включают информацию о HTTP методах
- Формат HAL для стандартизации ответов

### Отправляемые события Kafka:
- **Топик:** `user-events`
- **Содержимое:** email пользователя и тип операции (`CREATE` или `DELETE`)

### Интеграция с Spring Cloud:

- **Config Server**: Все настройки хранятся централизованно в `config-server`
- **Eureka Server**: Автоматическая регистрация и обнаружение сервисов
- **API Gateway**: Единая точка входа для всех запросов к API
- **Circuit Breaker**: Автоматическое переключение на fallback при недоступности сервиса

### Файлы конфигурации:
- `bootstrap.yml` - минимальная конфигурация для подключения к Config Server и Eureka
- Тестовые настройки остаются в `src/test/resources/application.properties`

---

## Тестирование

### Проект включает два типа тестов:

1. Интеграционные тесты (UserControllerTest)
    - Используют Testcontainers для запуска PostgreSQL в Docker
    - Тестируют весь стек приложения через MockMvc
    - Каждый тест выполняется в транзакции с автоматическим откатом
    - Проверяют корректность работы с БД и обработку исключений

2. Модульные тесты (UserServiceTest, GlobalExceptionHandlerTest, UserLinkBuilderTest)
    - Используют Mockito для изоляции тестируемых компонентов
    - Тестируют бизнес-логику, обработку исключений и построение HATEOAS ссылок

- Модульные тесты для UserLinkBuilder проверяют корректность генерации HATEOAS ссылок
- Покрытие всех возможных сценариев создания ссылок
- Проверка обработки граничных случаев (null значения)

---

### DTO

Приложение использует паттерн DTO для разделения слоев:

#### Request DTO:
- `UserRequest` - для создания и обновления пользователей с валидацией

#### Response DTO:
- `UserResponse` - базовый ответ с данными пользователя
- `UserResponseWithLinks` - ответ с данными и HATEOAS ссылками
- `UsersCollectionResponse` - ответ для коллекции пользователей со вложенными данными
- `DeleteResponse` - ответ после удаления с информационным сообщением и ссылками
- `ErrorResponse` - стандартизированный ответ при ошибках

#### Link DTO:
- `UserLinks` - набор HATEOAS ссылок для пользователя
- `CollectionSelfCreateLinks` - ссылки для коллекции
- `DeleteLinks` - ссылки после удаления

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

Host: localhost:8081  (прямой доступ)

Или через Gateway: http://localhost:8080/api/users

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
    "createdAt": "2024-01-15T10:30:00.000Z",
    "_links": {
        "self": { "href": "/api/users/1" },
        "update": { "href": "/api/users/1" },
        "delete": { "href": "/api/users/1" },
        "allUsers": { "href": "/api/users" },
        "create": { "href": "/api/users" }
        }
    }
---

### 2. Получение всех пользователей (GET)

**Запрос:**

GET /api/users

Host: localhost:8081  (прямой доступ)

Или через Gateway: http://localhost:8080/api/users

Accept: application/json

**Пример ответа (200 OK):**

    {
    "_embedded": {
        "userList": [
            {
                "id": 1,
                "name": "Иван Иванов",
                "email": "ivan@example.com",
                "age": 25,
                "createdAt": "2024-01-15T10:30:00.000Z",
                "_links": {
                    "self": { "href": "/api/users/1" }
                }
            },
            {
                "id": 2,
                "name": "Мария Петрова",
                "email": "maria@example.com",
                "age": 30,
                "createdAt": "2024-01-16T14:20:00.000Z",
                "_links": {
                    "self": { "href": "/api/users/2" }
                }
            }
        ]
    },
    "_links": {
        "self": { "href": "/api/users" },
        "create": { "href": "/api/users" }
        }
    }

---

### 3. Получение пользователя по ID (GET)

**Запрос:**

GET /api/users/1

Host: localhost:8081  (прямой доступ)

Или через Gateway: http://localhost:8080/api/users

Accept: application/json

**Пример ответа (200 OK):**

    {
    "id": 1,
    "name": "Иван Иванов",
    "email": "ivan@example.com",
    "age": 25,
    "createdAt": "2024-01-15T10:30:00.000Z",
    "_links": {
        "self": { "href": "/api/users/1" },
        "update": { "href": "/api/users/1" },
        "delete": { "href": "/api/users/1" },
        "allUsers": { "href": "/api/users" },
        "create": { "href": "/api/users" }
        }
    }

---

### 4. Обновление пользователя (PUT)

**Запрос:**

PUT /api/users/1

Host: localhost:8081  (прямой доступ)

Или через Gateway: http://localhost:8080/api/users

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
    "createdAt": "2024-01-15T10:30:00.000Z",
    "_links": {
        "self": { "href": "/api/users/1" },
        "update": { "href": "/api/users/1" },
        "delete": { "href": "/api/users/1" },
        "allUsers": { "href": "/api/users" },
        "create": { "href": "/api/users" }
        }
    }

---

### 5. Удаление пользователя (DELETE)

**Запрос:**

DELETE /api/users/1

Host: localhost:8081  (прямой доступ)

Или через Gateway: http://localhost:8080/api/users

**Пример ответа (204 No Content):**

    {
    "message": "Пользователь с ID 1 успешно удален",
    "timestamp": "2024-01-17T12:00:00.000Z",
    "_links": {
    "allUsers": { "href": "/api/users" },
    "create": { "href": "/api/users" }
        }
    }

---

### 6. Ошибка валидации (400 Bad Request)

**Некорректный запрос:**

POST /api/users

Host: localhost:8081  (прямой доступ)

Или через Gateway: http://localhost:8080/api/users

Content-Type: application/json

    {
        "name": "",
        "email": "invalid-email",
        "age": 150
    }

**Пример ответа (400 Bad Request):**

    {
    "message": "Ошибка валидации: {name=Имя не может быть пустым, email=Некорректный формат email, age=Возраст должен быть от 0 до 120}",
    "timestamp": "2024-01-17T12:00:00.000Z",
    "status": 400,
    "error": "Bad Request",
    "path": "/api/users"
    }

---

### 7. Пользователь не найден (400 Bad Request)


**Запрос:**

GET /api/users/999

Host: localhost:8081  (прямой доступ)

Или через Gateway: http://localhost:8080/api/users

Accept: application/json

**Пример ответа (400 Bad Request):**

    {
    "message": "Пользователь с ID 999 не найден",
    "timestamp": "2024-01-17T12:00:00.000Z",
    "status": 404,
    "error": "Not Found",
    "path": "/api/users/999"
    }

---

### Документация API

Документация генерируется автоматически с использованием SpringDoc OpenAPI.

### Доступные URL:
- **Через Gateway**: http://localhost:8080/swagger-ui.html
- **Прямой доступ**: http://localhost:8081/swagger-ui.html
- **OpenAPI спецификация**: http://localhost:8081/api-docs

Все эндпоинты документированы с описанием параметров, ответов и возможных ошибок.

---

### HATEOAS ссылки

Приложение реализует HATEOAS Level 3. Каждый ответ содержит ссылки на доступные действия:

#### Для индивидуального ресурса (/api/users/{id}):
- **self** - ссылка на самого себя (GET)
- **update** - ссылка для обновления (PUT)
- **delete** - ссылка для удаления (DELETE)
- **allUsers** - ссылка на список всех пользователей
- **create** - ссылка для создания нового пользователя

#### Для коллекции (/api/users):
- **self** - ссылка на коллекцию
- **create** - ссылка для создания нового элемента

#### После удаления:
- **allUsers** - ссылка на список всех пользователей
- **create** - ссылка для создания нового пользователя

---

### Порядок запуска системы:
1. **Eureka Server** (порт 8761): `cd eureka-server && mvn spring-boot:run`
2. **Config Server** (порт 8888): `cd config-server && mvn spring-boot:run`
3. **User Service** (порт 8081): `cd Module2 && mvn spring-boot:run`
4. **API Gateway** (порт 8080): `cd api-gateway && mvn spring-boot:run`

### Проверка работоспособности:
1. Проверьте Eureka Dashboard: http://localhost:8761
2. Убедитесь, что USER-SERVICE зарегистрирован
3. Проверьте API через Gateway: http://localhost:8080/api/users

---