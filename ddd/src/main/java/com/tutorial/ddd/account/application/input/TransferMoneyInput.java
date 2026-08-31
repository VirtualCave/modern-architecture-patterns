package com.tutorial.ddd.account.application.input;

import java.math.BigDecimal;

public record TransferMoneyInput(
        String fromAccountId,
        String toAccountId,
        BigDecimal amount
) {}
