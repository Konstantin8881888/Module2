package org.klimtsov.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:8081")
                .description("Локальный сервер разработки");

        return new OpenAPI()
                .servers(List.of(localServer))
                .info(new Info()
                        .title("User Service API")
                        .description("""
                            ## Микросервис управления пользователями
                            
                            CRUD API для управления пользователями с интеграцией:
                            - Создание, чтение, обновление, удаление пользователей
                            - Отправка событий в Kafka при изменениях
                            - Полноценная поддержка HATEOAS
                            
                            ## Особенности
                            - Гипермедиа ссылки (HATEOAS)
                            - Валидация входных данных
                            - Интеграция с PostgreSQL
                            - Коммуникация через Kafka
                            """));
    }
}