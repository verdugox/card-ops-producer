package com.bank.card_ops_producer.infrastructure.decorator;

import com.bank.card_ops_producer.domain.port.EventPublisher;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.rxjava3.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.rxjava3.retry.transformer.RetryTransformer;
import io.reactivex.rxjava3.core.Single;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
public class ResilientPublisher implements EventPublisher<Object> {
    private final EventPublisher<Object> delegate;
    private final CircuitBreaker cb;
    private final Retry retry;

    public ResilientPublisher(EventPublisher<Object> delegate) {
        this.delegate = delegate;
        this.cb = CircuitBreaker.ofDefaults("kafkaPublisher");
        this.retry = Retry.ofDefaults("kafkaPublisher");
    }

    @Override
    public Single<SendResult<String, Object>> publish(String topic, String key, Object value) {
        return delegate.publish(topic, key, value)
                .compose(CircuitBreakerOperator.of(cb))
                .compose(RetryTransformer.of(retry));
    }
}
