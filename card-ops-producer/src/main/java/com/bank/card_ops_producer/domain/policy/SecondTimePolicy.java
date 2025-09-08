package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import io.reactivex.rxjava3.core.Single;
import org.springframework.stereotype.Component;

@Component
public class SecondTimePolicy implements AttemptPolicy {
    @Override public io.reactivex.rxjava3.core.Single<Integer> resolveAttempt(CardReplacementRequestDto dto){ return io.reactivex.rxjava3.core.Single.just(2); }
    @Override public String name(){ return "secondTimePolicy"; }
}