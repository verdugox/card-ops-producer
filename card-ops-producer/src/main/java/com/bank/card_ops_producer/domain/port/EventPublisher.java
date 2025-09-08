package com.bank.card_ops_producer.domain.port;

import io.reactivex.rxjava3.core.Single;
import org.springframework.kafka.support.SendResult;

public interface EventPublisher<T> {
    Single<SendResult<String, T>> publish(String topic, String key, T value);
}
