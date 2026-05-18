package com.tutorial.ddd.account.domain;

public record AccountId(String value) {

    public AccountId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("account id is required");
        }
    }

    public static AccountId generate() {
        return new AccountId(java.util.UUID.randomUUID().toString());
    }

    public static AccountId of(String value) {
        return new AccountId(value);
    }
}

