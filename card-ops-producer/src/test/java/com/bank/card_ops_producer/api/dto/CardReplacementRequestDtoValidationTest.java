package com.bank.card_ops_producer.api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardReplacementRequestDtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void invalid_whenMissingRequiredFields() {
        var dto = CardReplacementRequestDto.builder().build();
        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void valid_whenAllRequiredProvided() {
        var dto = CardReplacementRequestDto.builder()
                .requestId("R1").customerId("C1").cardPANMasked("4111****9999")
                .reasonCode("DAMAGE").priority("HIGH").branchCode("BR1")
                .deliveryAddress("X").correlationId("CORR").status("REQUESTED")
                .build();
        var violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }
}
