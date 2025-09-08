// src/main/java/.../infrastructure/kafka/KafkaEventPublisher.java
package com.bank.card_ops_producer.infrastructure.kafka;

import com.bank.card_ops_producer.domain.port.EventPublisher;
import io.reactivex.rxjava3.core.Single;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component("kafkaEventPublisher")
public class KafkaEventPublisher implements EventPublisher<Object> {

    private final KafkaTemplate<String, Object> template;

    public KafkaEventPublisher(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    @Override
    public Single<SendResult<String, Object>> publish(String topic, String key, Object value) {
        var future = template.send(topic, key, value); // devuelve CompletableFuture
        return Single.create(emitter ->
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        if (!emitter.isDisposed()) emitter.onError(ex);
                    } else {
                        if (!emitter.isDisposed()) emitter.onSuccess(result);
                    }
                })
        );
    }
}

