package com.tutorial.ddd.account.infrastructure.rest.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositRequest(@Positive BigDecimal amount) {}

