package org.klimtsov.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.klimtsov.dto.UserEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "user-events";

    public void sendUserEvent(UserEvent userEvent) {
        log.info("Отправляю событие в Kafka: {}", userEvent);

        try {
            //Используем email в качестве ключа.
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(TOPIC, userEvent.getEmail(), userEvent);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Событие отправлено в Kafka. Топик: {}, Партиция: {}, Оффсет: {}",
                            TOPIC, result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Ошибка отправки в Kafka: {}", ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("❌ Критическая ошибка при отправке в Kafka: {}", e.getMessage());
        }
    }
}