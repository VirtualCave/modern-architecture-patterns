package com.tutorial.ddd.account.application.command;

import java.math.BigDecimal;

public record OpenAccountCommand(
        String ownerId,
        String ownerName,
        BigDecimal initialDeposit
) {}

