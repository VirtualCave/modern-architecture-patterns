package com.tutorial.ddd.account.application.command;

import java.math.BigDecimal;

public record TransferMoneyCommand(
        String fromAccountId,
        String toAccountId,
        BigDecimal amount
) {}

