package com.apexledger.account.adapter.in.web;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apexledger.account.application.ChangeAccountStatus;
import com.apexledger.account.application.CreateAccount;
import com.apexledger.account.application.GetAccount;
import com.apexledger.account.domain.Account;
import com.apexledger.account.domain.AccountId;
import com.apexledger.account.domain.AccountStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final CreateAccount createAccount;
    private final GetAccount getAccount;
    private final ChangeAccountStatus changeAccountStatus;

    public AccountController(CreateAccount createAccount, GetAccount getAccount, ChangeAccountStatus changeAccountStatus) {
        this.createAccount = createAccount;
        this.getAccount = getAccount;
        this.changeAccountStatus = changeAccountStatus;
    }

    @PostMapping
    @SuppressWarnings("unused")
    ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        Account account = createAccount.create(request.displayName());
        return ResponseEntity.created(URI.create("/api/v1/accounts/" + account.id())).body(AccountResponse.from(account));
    }

    @GetMapping("/{accountId}")
    @SuppressWarnings("unused")
    AccountResponse get(@PathVariable UUID accountId) {
        return AccountResponse.from(getAccount.get(new AccountId(accountId)));
    }

    @PutMapping("/{accountId}/status")
    @SuppressWarnings("unused")
    AccountResponse changeStatus(@PathVariable UUID accountId, @Valid @RequestBody ChangeStatusRequest request) {
        return AccountResponse.from(changeAccountStatus.change(new AccountId(accountId), request.status()));
    }

    public record CreateAccountRequest(
            @NotBlank(message = "displayName must contain 1 to 120 non-whitespace characters")
            @Size(max = 120, message = "displayName must contain 1 to 120 non-whitespace characters")
            String displayName) {

    }

    public record ChangeStatusRequest(@NotNull(message = "status is required") AccountStatus status) {

    }

    public record AccountResponse(UUID id, String displayName, AccountStatus status, Instant createdAt, Instant updatedAt) {

        static AccountResponse from(Account account) {
            return new AccountResponse(account.id().value(), account.displayName(), account.status(), account.createdAt(), account.updatedAt());
        }
    }
}
