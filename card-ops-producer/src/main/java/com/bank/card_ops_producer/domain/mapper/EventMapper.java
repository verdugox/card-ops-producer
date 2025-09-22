// src/main/java/com/bank/card_ops_producer/domain/mapper/EventMapper.java
package com.bank.card_ops_producer.domain.mapper;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.events.CardReplacementEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

//Transforma el CardReplacementRequestDto en el evento Avro CardReplacementEvent.
//Aquí ocurre la conversión del mundo HTTP → mundo eventos.
@Component
public class EventMapper {

    public CardReplacementEvent toEvent(CardReplacementRequestDto dto, int attemptNumber) {
        // Ajusta exactamente los nombres/tipos a tu AVSC
        return CardReplacementEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setRequestId(dto.getRequestId())
                .setCustomerId(dto.getCustomerId())
                .setCardPANMasked(dto.getCardPANMasked())
                .setReasonCode(dto.getReasonCode())
                .setPriority(dto.getPriority())
                .setBranchCode(dto.getBranchCode())
                .setDeliveryAddress(dto.getDeliveryAddress())
                .setRequestedAt(dto.getRequestedAt())  // ahora es Instant directo
                .setAttemptNumber(attemptNumber)
                .setCorrelationId(dto.getCorrelationId())
                .setStatus(dto.getStatus())
                .build();
    }
}
