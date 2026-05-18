package com.tutorial.ddd.account.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record OpenAccountRequest(
        @NotBlank String ownerId,
        @NotBlank String ownerName,
        @PositiveOrZero BigDecimal initialDeposit
) {}

