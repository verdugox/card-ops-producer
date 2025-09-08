// src/main/java/com/bank/card_ops_producer/domain/policy/AttemptPolicyOrchestrator.java
package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.port.AttemptStateRepository;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class AttemptPolicyOrchestrator implements AttemptPolicy {

    private final FirstTimePolicy first;
    private final SecondTimePolicy second;
    private final AttemptStateRepository repo;

    @Override
    public io.reactivex.rxjava3.core.Single<Integer> resolveAttempt(CardReplacementRequestDto dto) {
        final String requestId = dto.getRequestId();
        return repo.existsByRequestId(requestId)
                .flatMap(exists -> exists
                        ? second.resolveAttempt(dto)                  // segunda vez
                        : repo.saveFirstAttempt(requestId)            // primera vez
                        .onErrorReturnItem(false)
                        .flatMap(ignored -> first.resolveAttempt(dto)));
    }

    @Override
    public String name() {
        return "orchestrator";
    }
}

