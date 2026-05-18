package com.tutorial.ddd.account;

import com.tutorial.ddd.account.domain.Account;
import com.tutorial.ddd.account.domain.AccountId;
import com.tutorial.ddd.account.domain.exception.InsufficientFundsException;
import com.tutorial.ddd.shared.domain.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountDomainTest {

    @Test
    void transferShouldMoveBalance() {
        var a = Account.open(AccountId.generate(), "C1", "Alice", Money.euros(100));
        var b = Account.open(AccountId.generate(), "C2", "Bob", Money.euros(20));

        a.transferTo(b, Money.euros(30));

        assertThat(a.balance().amount()).isEqualByComparingTo("70.00");
        assertThat(b.balance().amount()).isEqualByComparingTo("50.00");
    }

    @Test
    void withdrawShouldFailWhenInsufficient() {
        var a = Account.open(AccountId.generate(), "C1", "Alice", Money.euros(10));

        assertThatThrownBy(() -> a.withdraw(Money.euros(20)))
                .isInstanceOf(InsufficientFundsException.class);
    }
}

