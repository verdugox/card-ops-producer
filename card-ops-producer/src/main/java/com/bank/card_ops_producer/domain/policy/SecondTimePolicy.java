package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import io.reactivex.rxjava3.core.Single;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

@Component("secondTimePolicy")
public class SecondTimePolicy implements AttemptPolicy {
    private final ReactiveStringRedisTemplate redis;

    public SecondTimePolicy(ReactiveStringRedisTemplate redis){ this.redis = redis; }

    @Override
    public Single<Integer> resolveAttempt(CardReplacementRequestDto dto) {
        String key = "card:replacement:req:" + dto.getRequestId();
        // Si existe → 2; si no existe aún, forzamos 1 (fallback)
        return RxJava3Adapter.monoToSingle(
                redis.hasKey(key).map(exists -> exists ? 2 : 1)
        );
    }

    @Override public String name(){ return "SECOND_TIME_POLICY"; }
}
