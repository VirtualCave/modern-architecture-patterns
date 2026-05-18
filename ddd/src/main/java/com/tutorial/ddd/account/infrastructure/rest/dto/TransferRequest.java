package com.tutorial.ddd.account.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String toAccountId,
        @Positive BigDecimal amount
) {}

