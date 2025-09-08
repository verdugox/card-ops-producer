// src/main/java/com/bank/card_ops_producer/infrastructure/redis/RedisAttemptStateRepository.java
package com.bank.card_ops_producer.infrastructure.redis;

import com.bank.card_ops_producer.domain.port.AttemptStateRepository;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisAttemptStateRepository implements AttemptStateRepository {

    private final ReactiveStringRedisTemplate redis; // 👈 usa el autoconfigurado

    private static final String PREFIX = "card:req:";

    @Override
    public Single<Boolean> existsByRequestId(String requestId) {
        return Single.fromPublisher(redis.hasKey(PREFIX + requestId));
    }

    @Override
    public Single<Boolean> saveFirstAttempt(String requestId) {
        return Single.fromPublisher(redis.opsForValue().set(PREFIX + requestId, "1"));
    }
}
