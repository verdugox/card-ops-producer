package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import io.reactivex.rxjava3.core.Single;

public interface AttemptPolicy {
    io.reactivex.rxjava3.core.Single<Integer> resolveAttempt(com.bank.card_ops_producer.api.dto.CardReplacementRequestDto dto);
    String name(); // <- existe y es abstracto
}
