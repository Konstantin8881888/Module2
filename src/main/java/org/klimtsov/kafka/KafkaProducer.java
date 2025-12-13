package org.klimtsov.kafka;

import lombok.RequiredArgsConstructor;
import org.klimtsov.dto.UserEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "user-events";

    public void sendUserEvent(UserEvent userEvent) {
        kafkaTemplate.send(TOPIC, userEvent);
    }
}