package com.bank.card_ops_producer.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RedisAttemptStateRepositoryTest {

    private ReactiveStringRedisTemplate redis;
    private ReactiveValueOperations<String, String> ops;
    private RedisAttemptStateRepository repo;

    @BeforeEach
    void setUp() {
        redis = mock(ReactiveStringRedisTemplate.class);
        //noinspection unchecked
        ops = (ReactiveValueOperations<String, String>) mock(ReactiveValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        repo = new RedisAttemptStateRepository(redis);
    }

    @Test
    void existsByRequestId_trueWhenPresent() {
        when(redis.hasKey("card:req:REQ-1")).thenReturn(Mono.just(true));

        boolean exists = repo.existsByRequestId("REQ-1").blockingGet();

        assertThat(exists).isTrue();
        verify(redis).hasKey("card:req:REQ-1");
    }

    @Test
    void existsByRequestId_falseWhenEmpty() {
        when(redis.hasKey("card:req:REQ-2")).thenReturn(Mono.just(false));

        boolean exists = repo.existsByRequestId("REQ-2").blockingGet();

        assertThat(exists).isFalse();
        verify(redis).hasKey("card:req:REQ-2");
    }

    @Test
    void saveFirstAttempt_setsTo1() {
        when(ops.set("card:req:REQ-3", "1")).thenReturn(Mono.just(true));

        repo.saveFirstAttempt("REQ-3").test().assertValue(true);

        verify(ops).set("card:req:REQ-3", "1");
    }

    @Test
    void saveEventSnapshot_setsKeyWithTTL() {
        when(ops.set(eq("card:event:REQ-4"), eq("{json}"), any()))
                .thenReturn(Mono.just(true));

        repo.saveEventSnapshot("REQ-4", "{json}", java.time.Duration.ofSeconds(30))
                .test()
                .assertValue(true);

        verify(ops).set(eq("card:event:REQ-4"), eq("{json}"), any());
    }
}
