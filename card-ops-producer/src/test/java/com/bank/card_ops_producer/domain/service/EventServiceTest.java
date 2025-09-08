package com.bank.card_ops_producer.domain.service;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.mapper.EventMapper;
import com.bank.card_ops_producer.domain.policy.AttemptPolicy;
import com.bank.card_ops_producer.domain.port.EventPublisher;
import com.bank.events.CardReplacementEvent;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

public class EventServiceTest {

    @Test
    void publishesEventAndReturnsEventId() {
        AttemptPolicy policy = Mockito.mock(AttemptPolicy.class);
        EventMapper mapper = Mockito.mock(EventMapper.class);
        @SuppressWarnings("unchecked")
        EventPublisher<Object> publisher = Mockito.mock(EventPublisher.class);

        var dto = CardReplacementRequestDto.builder()
                .requestId("R1").customerId("C1").cardPANMasked("4111****1111")
                .reasonCode("LOST").priority("HIGH").branchCode("001")
                .deliveryAddress("Av. Siempre Viva 742")
                .correlationId("corr-123").status("REQUESTED")
                .build();

        // 1) policy -> primera vez
        Mockito.when(policy.resolveAttempt(dto)).thenReturn(Single.just(1));

        // 2) evento Avro usa Instant en requestedAt
        var evt = new CardReplacementEvent(
                "E1", "R1", "C1", "4111****1111",
                "LOST", "HIGH", "001", "Av. Siempre Viva 742",
                Instant.now(), // 👈 en lugar de System.currentTimeMillis()
                1, "corr-123", "REQUESTED"
        );
        Mockito.when(mapper.toEvent(dto, 1)).thenReturn(evt);

        // 3) publisher devuelve Single<SendResult<...>> (¡no null!)
        @SuppressWarnings("unchecked")
        SendResult<String, Object> sendResult = Mockito.mock(SendResult.class);
        Mockito.when(publisher.publish(anyString(), eq("R1"), eq(evt)))
                .thenReturn(Single.just(sendResult));

        // SUT
        EventService service = new EventService(policy, mapper, publisher, "bank.card.replacements.v1");

        // Act
        String id = service.process(dto).blockingGet();

        // Assert
        assertThat(id).isEqualTo("E1");
    }
}
