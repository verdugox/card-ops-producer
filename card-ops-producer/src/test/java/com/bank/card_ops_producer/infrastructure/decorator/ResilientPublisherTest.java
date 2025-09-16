package com.bank.card_ops_producer.infrastructure.decorator;

import com.bank.card_ops_producer.domain.port.EventPublisher;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.SendResult;

import static org.mockito.Mockito.*;

class ResilientPublisherTest {

    @Test
    void publish_delegatesToInnerPublisher() {
        // Arrange
        @SuppressWarnings("unchecked")
        EventPublisher<Object> delegate = (EventPublisher<Object>) mock(EventPublisher.class);
        var resilient = new ResilientPublisher(delegate);

        when(delegate.publish("topic", "key", "value"))
                .thenReturn(Single.just(mock(SendResult.class)));

        // Act + Assert
        resilient.publish("topic", "key", "value").test().assertComplete();
        verify(delegate).publish("topic", "key", "value");
    }
}
