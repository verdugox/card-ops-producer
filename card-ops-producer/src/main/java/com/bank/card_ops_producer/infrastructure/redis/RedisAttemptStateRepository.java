// src/main/java/com/bank/card_ops_producer/infrastructure/redis/RedisAttemptStateRepository.java
package com.bank.card_ops_producer.infrastructure.redis;

import com.bank.card_ops_producer.domain.port.AttemptStateRepository;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava3Adapter;   // 👈 IMPORTANTE

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisAttemptStateRepository implements AttemptStateRepository {

    private final ReactiveStringRedisTemplate redis;

    @Override
    public Single<Boolean> existsByRequestId(String requestId) {
        return RxJava3Adapter.monoToSingle(
                redis.hasKey(key(requestId))               // Mono<Boolean>
                        .map(Boolean::booleanValue)
        );
    }

    @Override
    public Single<Boolean> saveFirstAttempt(String requestId) {
        return RxJava3Adapter.monoToSingle(
                redis.opsForValue().set(key(requestId), "1") // Mono<Boolean>
        );
    }

    @Override
    public Single<Boolean> saveEventSnapshot(String requestId, String json, Duration ttl) {
        return RxJava3Adapter.monoToSingle(
                redis.opsForValue().set(snapKey(requestId), json, ttl) // Mono<Boolean>
        );
    }

    private String key(String id)     { return "card:req:"   + id; }
    private String snapKey(String id) { return "card:event:" + id; }
}
