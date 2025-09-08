package com.bank.card_ops_producer.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Data
@Builder
public class CardReplacementRequestDto {
    @NotBlank private String requestId;
    @NotBlank private String customerId;
    @NotBlank private String cardPANMasked; // 16****9999
    @NotBlank private String reasonCode;    // LOST/DAMAGED/UPGRADE
    @NotBlank private String priority;      // HIGH/NORMAL
    @NotBlank private String branchCode;
    @NotBlank private String deliveryAddress;
    @Builder.Default private Instant requestedAt = Instant.now();
    @NotBlank private String correlationId;
    @NotBlank private String status;        // REQUESTED, VALIDATED, etc.
}
