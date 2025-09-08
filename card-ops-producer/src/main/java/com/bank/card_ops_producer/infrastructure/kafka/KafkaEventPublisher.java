package com.bank.card_ops_producer.infrastructure.kafka;

import com.bank.card_ops_producer.domain.port.EventPublisher;
import io.reactivex.rxjava3.core.Single;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaEventPublisher implements EventPublisher<Object> {

    private final KafkaTemplate<String, Object> template;

    public KafkaEventPublisher(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    @Override
    public Single<SendResult<String, Object>> publish(String topic, String key, Object value) {
        // En Spring Kafka 3.3+ esto ya es CompletableFuture<SendResult<String,Object>>
        CompletableFuture<SendResult<String, Object>> future = template.send(topic, key, value);
        // RxJava lo envuelve directo (CompletionStage)
        return Single.fromCompletionStage(future);
    }
}
