package com.tutorial.ddd.account.domain.repository;

import com.tutorial.ddd.account.domain.Account;
import com.tutorial.ddd.account.domain.AccountId;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(AccountId id);
    List<Account> findByOwnerId(String ownerId);
}

