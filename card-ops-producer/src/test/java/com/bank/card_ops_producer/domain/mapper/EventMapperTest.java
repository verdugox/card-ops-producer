package com.bank.card_ops_producer.domain.mapper;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.events.CardReplacementEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    private final EventMapper mapper = new EventMapper();

    @Test
    void toEvent_mapsAllFields_andGeneratesEventId() {
        // Arrange
        Instant now = Instant.parse("2025-01-01T10:00:00Z");
        var dto = CardReplacementRequestDto.builder()
                .requestId("REQ-1")
                .customerId("C-123")
                .cardPANMasked("4111****9999")
                .reasonCode("DAMAGE")
                .priority("HIGH")
                .branchCode("BR-01")
                .deliveryAddress("Av. Siempre Viva 742")
                .requestedAt(now)
                .correlationId("CORR-1")
                .status("REQUESTED")
                .build();

        // Act
        CardReplacementEvent evt = mapper.toEvent(dto, 2);

        // Assert
        assertThat(evt.getEventId()).isNotBlank();
        assertThat(evt.getRequestId()).isEqualTo("REQ-1");
        assertThat(evt.getCustomerId()).isEqualTo("C-123");
        assertThat(evt.getCardPANMasked()).isEqualTo("4111****9999");
        assertThat(evt.getReasonCode()).isEqualTo("DAMAGE");
        assertThat(evt.getPriority()).isEqualTo("HIGH");
        assertThat(evt.getBranchCode()).isEqualTo("BR-01");
        assertThat(evt.getDeliveryAddress()).isEqualTo("Av. Siempre Viva 742");
        assertThat(evt.getRequestedAt()).isEqualTo(now);
        assertThat(evt.getAttemptNumber()).isEqualTo(2);
        assertThat(evt.getCorrelationId()).isEqualTo("CORR-1");
        assertThat(evt.getStatus()).isEqualTo("REQUESTED");
    }
}
