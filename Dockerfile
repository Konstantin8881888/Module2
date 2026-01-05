# Используем официальный образ OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл
COPY target/*.jar app.jar

# Открываем порт
EXPOSE ${SERVER_PORT}

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]