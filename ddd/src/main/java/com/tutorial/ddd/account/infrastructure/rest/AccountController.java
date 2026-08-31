package com.tutorial.ddd.account.infrastructure.rest;

import com.tutorial.ddd.account.application.AccountApplicationService;
import com.tutorial.ddd.account.application.input.DepositMoneyInput;
import com.tutorial.ddd.account.application.input.OpenAccountInput;
import com.tutorial.ddd.account.application.input.TransferMoneyInput;
import com.tutorial.ddd.account.domain.Account;
import com.tutorial.ddd.account.infrastructure.rest.dto.DepositRequest;
import com.tutorial.ddd.account.infrastructure.rest.dto.OpenAccountRequest;
import com.tutorial.ddd.account.infrastructure.rest.dto.TransferRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountApplicationService service;


    @PostMapping
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        var account = service.openAccount(new OpenAccountInput(
                request.ownerId(),
                request.ownerName(),
                request.initialDeposit()
        ));
        return ResponseEntity.created(URI.create("/api/accounts/" + account.id().value())).body(AccountResponse.from(account));
    }

    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable String id) {
        return AccountResponse.from(service.getById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public List<AccountResponse> byOwner(@PathVariable String ownerId) {
        return service.findByOwner(ownerId).stream().map(AccountResponse::from).toList();
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable String id, @Valid @RequestBody DepositRequest request) {
        var updated = service.deposit(new DepositMoneyInput(id, request.amount()));
        return AccountResponse.from(updated);
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<Void> transfer(@PathVariable String id, @Valid @RequestBody TransferRequest request) {
        service.transfer(new TransferMoneyInput(id, request.toAccountId(), request.amount()));
        return ResponseEntity.accepted().build();
    }

    public record AccountResponse(String id, String ownerId, String ownerName, String balance, String currency) {
        static AccountResponse from(Account account) {
            return new AccountResponse(
                    account.id().value(),
                    account.ownerId(),
                    account.ownerName(),
                    account.balance().amount().toPlainString(),
                    account.balance().currency()
            );
        }
    }
}

