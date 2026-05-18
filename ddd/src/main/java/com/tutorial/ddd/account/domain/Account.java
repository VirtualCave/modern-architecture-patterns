package com.tutorial.ddd.account.domain;

import com.tutorial.ddd.account.domain.event.AccountEvent;
import com.tutorial.ddd.account.domain.exception.InsufficientFundsException;
import com.tutorial.ddd.shared.domain.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Account {

    private final AccountId id;
    private final String ownerId;
    private String ownerName;
    private Money balance;
    private boolean active;
    private final LocalDateTime createdAt;
    private final List<AccountEvent> domainEvents = new ArrayList<>();

    private Account(AccountId id, String ownerId, String ownerName, Money initialDeposit) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.balance = initialDeposit;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public static Account open(AccountId id, String ownerId, String ownerName, Money initialDeposit) {
        if (ownerId == null || ownerId.isBlank()) throw new IllegalArgumentException("ownerId is required");
        if (ownerName == null || ownerName.isBlank()) throw new IllegalArgumentException("ownerName is required");
        var account = new Account(id, ownerId, ownerName, initialDeposit);
        account.domainEvents.add(new AccountEvent.AccountOpened(id.value(), ownerId, initialDeposit));
        return account;
    }

    public static Account reconstitute(AccountId id, String ownerId, String ownerName, Money balance, boolean active, LocalDateTime createdAt) {
        var account = new Account(id, ownerId, ownerName, balance);
        account.active = active;
        return account;
    }

    public void deposit(Money amount) {
        assertActive();
        this.balance = this.balance.add(amount);
        domainEvents.add(new AccountEvent.MoneyDeposited(id.value(), amount));
    }

    public void withdraw(Money amount) {
        assertActive();
        try {
            this.balance = this.balance.subtract(amount);
        } catch (IllegalArgumentException e) {
            throw new InsufficientFundsException("Insufficient funds in account " + id.value());
        }
        domainEvents.add(new AccountEvent.MoneyWithdrawn(id.value(), amount));
    }

    public void transferTo(Account destination, Money amount) {
        if (destination.id.equals(this.id)) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }
        this.withdraw(amount);
        destination.deposit(amount);
        domainEvents.add(new AccountEvent.MoneyTransferred(this.id.value(), destination.id.value(), amount));
    }

    public List<AccountEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    private void assertActive() {
        if (!active) throw new IllegalStateException("Account is not active");
    }

    public AccountId id() { return id; }
    public String ownerId() { return ownerId; }
    public String ownerName() { return ownerName; }
    public Money balance() { return balance; }
    public boolean isActive() { return active; }
    public LocalDateTime createdAt() { return createdAt; }
}

