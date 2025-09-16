package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SecondTimePolicyTest {

    @Test
    void resolveAttempt_returns2() {
        // Arrange
        var policy = new SecondTimePolicy();
        var dto = CardReplacementRequestDto.builder().requestId("R2").build();

        // Act
        int attempt = policy.resolveAttempt(dto).blockingGet();

        // Assert
        assertThat(attempt).isEqualTo(2);
    }
}
