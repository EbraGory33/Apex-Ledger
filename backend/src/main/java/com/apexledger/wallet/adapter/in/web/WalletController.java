package com.apexledger.wallet.adapter.in.web;

import com.apexledger.account.domain.AccountId;
import com.apexledger.wallet.application.ChangeWalletStatus;
import com.apexledger.wallet.application.CreateWallet;
import com.apexledger.wallet.application.GetWallet;
import com.apexledger.wallet.application.ListAccountWallets;
import com.apexledger.wallet.domain.Wallet;
import com.apexledger.wallet.domain.WalletId;
import com.apexledger.wallet.domain.WalletStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class WalletController {
    private final CreateWallet createWallet;
    private final GetWallet getWallet;
    private final ListAccountWallets listAccountWallets;
    private final ChangeWalletStatus changeWalletStatus;

    public WalletController(CreateWallet createWallet, GetWallet getWallet,
            ListAccountWallets listAccountWallets, ChangeWalletStatus changeWalletStatus) {
        this.createWallet = createWallet;
        this.getWallet = getWallet;
        this.listAccountWallets = listAccountWallets;
        this.changeWalletStatus = changeWalletStatus;
    }

    @PostMapping("/accounts/{accountId}/wallets")
    ResponseEntity<WalletResponse> create(@PathVariable UUID accountId, @Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = createWallet.create(new AccountId(accountId), request.label());
        return ResponseEntity.created(URI.create("/api/v1/wallets/" + wallet.id())).body(WalletResponse.from(wallet));
    }

    @GetMapping("/wallets/{walletId}")
    WalletResponse get(@PathVariable UUID walletId) {
        return WalletResponse.from(getWallet.get(new WalletId(walletId)));
    }

    @GetMapping("/accounts/{accountId}/wallets")
    WalletListResponse list(@PathVariable UUID accountId) {
        List<WalletResponse> wallets = listAccountWallets.list(new AccountId(accountId)).stream()
                .map(WalletResponse::from).toList();
        return new WalletListResponse(wallets);
    }

    @PutMapping("/wallets/{walletId}/status")
    WalletResponse changeStatus(@PathVariable UUID walletId, @Valid @RequestBody ChangeStatusRequest request) {
        return WalletResponse.from(changeWalletStatus.change(new WalletId(walletId), request.status()));
    }

    public record CreateWalletRequest(
            @NotBlank(message = "label must contain 1 to 80 non-whitespace characters")
            @Size(max = 80, message = "label must contain 1 to 80 non-whitespace characters") String label) { }
    public record ChangeStatusRequest(@NotNull(message = "status is required") WalletStatus status) { }
    public record WalletListResponse(List<WalletResponse> items) { }
    public record WalletResponse(UUID id, UUID accountId, String label, String assetCode, WalletStatus status,
            Instant createdAt, Instant updatedAt) {
        static WalletResponse from(Wallet wallet) {
            return new WalletResponse(wallet.id().value(), wallet.accountId().value(), wallet.label(), wallet.assetCode(),
                    wallet.status(), wallet.createdAt(), wallet.updatedAt());
        }
    }
}
