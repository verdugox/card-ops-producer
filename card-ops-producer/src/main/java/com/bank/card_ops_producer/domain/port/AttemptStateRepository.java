// src/main/java/com/bank/card_ops_producer/domain/port/AttemptStateRepository.java
package com.bank.card_ops_producer.domain.port;

import io.reactivex.rxjava3.core.Single;

public interface AttemptStateRepository {
    Single<Boolean> existsByRequestId(String requestId); // ¿ya hubo intento?
    Single<Boolean> saveFirstAttempt(String requestId);  // marca 1er intento
}
