package com.bank.card_ops_producer.domain.port;

import io.reactivex.rxjava3.core.Single;
import org.springframework.kafka.support.SendResult;

//Define la acción de publicar un evento
public interface EventPublisher<T> {
    Single<SendResult<String, T>> publish(String topic, String key, T value);
}
