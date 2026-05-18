package com.tutorial.ddd.account.domain.service;

import com.tutorial.ddd.account.domain.Account;
import com.tutorial.ddd.shared.domain.Money;

public class TransferDomainService {

    public void transfer(Account from, Account to, Money amount) {
        from.transferTo(to, amount);
    }
}

