package com.tutorial.ddd.account.application.input;

import java.math.BigDecimal;

public record OpenAccountInput(
        String ownerId,
        String ownerName,
        BigDecimal initialDeposit
) {}
