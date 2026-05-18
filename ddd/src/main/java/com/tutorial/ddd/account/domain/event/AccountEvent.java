package com.tutorial.ddd.account.domain.event;

import com.tutorial.ddd.shared.domain.Money;

public sealed interface AccountEvent permits
        AccountEvent.AccountOpened,
        AccountEvent.MoneyDeposited,
        AccountEvent.MoneyWithdrawn,
        AccountEvent.MoneyTransferred {

    record AccountOpened(String accountId, String ownerId, Money initialBalance) implements AccountEvent {}
    record MoneyDeposited(String accountId, Money amount) implements AccountEvent {}
    record MoneyWithdrawn(String accountId, Money amount) implements AccountEvent {}
    record MoneyTransferred(String fromAccountId, String toAccountId, Money amount) implements AccountEvent {}
}

