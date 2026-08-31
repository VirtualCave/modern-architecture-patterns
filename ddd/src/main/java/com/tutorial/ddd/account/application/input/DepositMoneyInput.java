package com.tutorial.ddd.account.application.input;

import java.math.BigDecimal;

public record DepositMoneyInput(String accountId, BigDecimal amount) {}
