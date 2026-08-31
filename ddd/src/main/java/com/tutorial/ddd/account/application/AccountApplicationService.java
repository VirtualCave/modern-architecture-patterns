package com.tutorial.ddd.account.application;

import com.tutorial.ddd.account.application.input.DepositMoneyInput;
import com.tutorial.ddd.account.application.input.OpenAccountInput;
import com.tutorial.ddd.account.application.input.TransferMoneyInput;
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

    public Account openAccount(OpenAccountInput input) {
        var account = Account.open(
                AccountId.generate(),
                input.ownerId(),
                input.ownerName(),
                new Money(input.initialDeposit(), "EUR")
        );
        return repository.save(account);
    }

    public Account deposit(DepositMoneyInput input) {
        var account = repository.findById(AccountId.of(input.accountId()))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + input.accountId()));
        account.deposit(new Money(input.amount(), "EUR"));
        var saved = repository.save(account);
        // Nota: Account acumula domain events internamente con pullDomainEvents().
        // En este módulo DDD simplificado no los publicamos a ningún bus externo.
        // El módulo hybrid muestra el ciclo completo: save → project → publish events.
        return saved;
    }

    public void transfer(TransferMoneyInput input) {
        var from = repository.findById(AccountId.of(input.fromAccountId()))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + input.fromAccountId()));
        var to = repository.findById(AccountId.of(input.toAccountId()))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + input.toAccountId()));

        transferDomainService.transfer(from, to, new Money(input.amount(), "EUR"));

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

