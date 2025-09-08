package com.bank.card_ops_producer.domain.service;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.mapper.EventMapper;
import com.bank.card_ops_producer.domain.policy.AttemptPolicy;
import com.bank.card_ops_producer.domain.port.EventPublisher;
import com.bank.events.CardReplacementEvent;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final AttemptPolicy policy;
    private final EventMapper mapper;
    private final EventPublisher<Object> publisher;
    private final String topic;

    public EventService(
            AttemptPolicy policy,
            EventMapper mapper,
            EventPublisher<Object> publisher,
            @Value("${kafka.topic}") String topic) {
        this.policy = policy;
        this.mapper = mapper;
        this.publisher = publisher;
        this.topic = topic;
    }

    public Single<String> process(CardReplacementRequestDto dto) {
        return policy.resolveAttempt(dto)                            // Single<Integer>
                .map(attempt -> mapper.toEvent(dto, attempt))            // Single<CardReplacementEvent>
                .flatMap(evt -> publisher.publish(topic, evt.getRequestId(), evt)
                        .map(sr -> evt.getEventId())); // Single<String>
    }
}
