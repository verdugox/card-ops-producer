package com.bank.card_ops_producer.domain.mapper;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.events.CardReplacementEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class})
public interface EventMapper {

    @Mapping(target = "eventId", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "attemptNumber", source = "attemptNumber")
        // requestedAt ya es Instant en el DTO y en el Avro → no lo toques
    CardReplacementEvent toEvent(CardReplacementRequestDto dto, int attemptNumber);
}
