package com.bank.card_ops_producer.domain.policy;

import io.reactivex.rxjava3.core.Single;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component("firstTimePolicy")
public class FirstTimePolicy implements AttemptPolicy {

    private final ReactiveStringRedisTemplate redis;

    public FirstTimePolicy(ReactiveStringRedisTemplate redis) { this.redis = redis; }

    @Override
    public Single<Integer> resolveAttempt(CardReplacementRequestDto dto) {
        String key = "card:replacement:req:" + dto.getRequestId();
        Mono<Boolean> set = redis.opsForValue().setIfAbsent(key, "1", Duration.ofHours(24));
        return RxJava3Adapter.monoToSingle(set.map(created -> created ? 1 : 2));
    }

    @Override public String name(){ return "FIRST_TIME_POLICY"; }
}
