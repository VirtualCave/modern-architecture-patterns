package com.tutorial.ddd.account.application;

import com.tutorial.ddd.account.application.command.DepositMoneyCommand;
import com.tutorial.ddd.account.application.command.OpenAccountCommand;
import com.tutorial.ddd.account.application.command.TransferMoneyCommand;
import com.tutorial.ddd.account.domain.Account;
import com.tutorial.ddd.account.domain.AccountId;
import com.tutorial.ddd.account.domain.repository.AccountRepository;
import com.tutorial.ddd.account.domain.service.TransferDomainService;
import com.tutorial.ddd.shared.domain.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountApplicationService {

    private final AccountRepository repository;
    private final TransferDomainService transferDomainService = new TransferDomainService();

    public Account openAccount(OpenAccountCommand command) {
        var account = Account.open(
                AccountId.generate(),
                command.ownerId(),
                command.ownerName(),
                new Money(command.initialDeposit(), "EUR")
        );
        return repository.save(account);
    }

    public Account deposit(DepositMoneyCommand command) {
        var account = repository.findById(AccountId.of(command.accountId()))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.accountId()));
        account.deposit(new Money(command.amount(), "EUR"));
        return repository.save(account);
    }

    public void transfer(TransferMoneyCommand command) {
        var from = repository.findById(AccountId.of(command.fromAccountId()))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.fromAccountId()));
        var to = repository.findById(AccountId.of(command.toAccountId()))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.toAccountId()));

        transferDomainService.transfer(from, to, new Money(command.amount(), "EUR"));

        repository.save(from);
        repository.save(to);
    }

    @Transactional(readOnly = true)
    public Account getById(String accountId) {
        return repository.findById(AccountId.of(accountId))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    @Transactional(readOnly = true)
    public List<Account> findByOwner(String ownerId) {
        return repository.findByOwnerId(ownerId);
    }
}

