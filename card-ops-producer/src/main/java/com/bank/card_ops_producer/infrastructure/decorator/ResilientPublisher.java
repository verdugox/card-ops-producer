// src/main/java/.../infrastructure/decorator/ResilientPublisher.java
package com.bank.card_ops_producer.infrastructure.decorator;

import com.bank.card_ops_producer.domain.port.EventPublisher;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.SendResult;

@Component("resilientPublisher")
@Primary // 👈 este será el bean preferido para EventPublisher<Object>
@RequiredArgsConstructor
public class ResilientPublisher implements EventPublisher<Object> {

    // Inyecta explícitamente el publisher “real” de Kafka
    private final @Qualifier("kafkaEventPublisher") EventPublisher<Object> delegate;

    @Override
    @CircuitBreaker(name = "kafkaPublisher")
    @Retry(name = "kafkaPublisher")
    public Single<SendResult<String, Object>> publish(String topic, String key, Object value) {
        return delegate.publish(topic, key, value);
    }
}
