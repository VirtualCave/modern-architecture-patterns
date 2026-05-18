package com.tutorial.ddd.account.application.command;

import java.math.BigDecimal;

public record DepositMoneyCommand(String accountId, BigDecimal amount) {}

