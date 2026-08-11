package com.apexledger.ledger.adapter.in.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apexledger.ledger.application.CreateJournalDraft;
import com.apexledger.ledger.application.GetWalletBalance;
import com.apexledger.ledger.application.PostJournalEntry;
import com.apexledger.ledger.application.ProvisionLedgerAccount;
import com.apexledger.ledger.application.ReverseJournalEntry;
import com.apexledger.ledger.domain.journal.JournalEntry;
import com.apexledger.ledger.domain.journal.JournalEntryId;
import com.apexledger.ledger.domain.ledger.EntryDirection;
import com.apexledger.ledger.domain.ledger.LedgerAccount;
import com.apexledger.ledger.domain.ledger.LedgerAccountId;
import com.apexledger.ledger.domain.ledger.LedgerEntry;
import com.apexledger.wallet.domain.WalletId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1")
public class LedgerController {

    private final ProvisionLedgerAccount provision;
    private final GetWalletBalance balances;
    private final CreateJournalDraft drafts;
    private final PostJournalEntry posting;
    private final ReverseJournalEntry reversals;

    public LedgerController(ProvisionLedgerAccount provision, GetWalletBalance balances, CreateJournalDraft drafts,
            PostJournalEntry posting, ReverseJournalEntry reversals) {
        this.provision = provision;
        this.balances = balances;
        this.drafts = drafts;
        this.posting = posting;
        this.reversals = reversals;
    }

    @PostMapping("/internal/ledger-accounts")
    @SuppressWarnings("unused")
    ResponseEntity<LedgerAccountResponse> provision(@Valid @RequestBody ProvisionRequest request) {
        LedgerAccount account = provision.provision(new WalletId(request.walletId()));
        return ResponseEntity.ok(LedgerAccountResponse.from(account));
    }

    @GetMapping("/wallets/{walletId}/balance")
    @SuppressWarnings("unused")
    BalanceResponse balance(@PathVariable UUID walletId) {
        GetWalletBalance.Balance balance = balances.get(new WalletId(walletId));
        return new BalanceResponse(balance.walletId().value(), "APX", balance.balanceAtomic());
    }

    @PostMapping("/internal/journal-entries")
    @SuppressWarnings("unused")
    ResponseEntity<JournalResponse> draft(@Valid @RequestBody CreateJournalRequest request) {
        List<CreateJournalDraft.Line> lines = request.lines().stream().map(line -> new CreateJournalDraft.Line(
                new LedgerAccountId(line.ledgerAccountId()), line.direction(), line.amountAtomic())).toList();
        JournalEntry journal = drafts.create(request.referenceType(), request.referenceId(), lines);
        return ResponseEntity.status(201).body(JournalResponse.from(journal));
    }

    @PostMapping("/internal/journal-entries/{journalEntryId}/post")
    @SuppressWarnings("unused")
    JournalResponse post(@PathVariable UUID journalEntryId) {
        return JournalResponse.from(posting.post(new JournalEntryId(journalEntryId)));
    }

    @PostMapping("/internal/journal-entries/{journalEntryId}/reverse")
    @SuppressWarnings("unused")
    JournalResponse reverse(@PathVariable UUID journalEntryId) {
        return JournalResponse.from(reversals.reverse(new JournalEntryId(journalEntryId)));
    }

    public record ProvisionRequest(@NotNull UUID walletId) {

    }

    public record CreateJournalRequest(@NotBlank String referenceType, String referenceId, @NotEmpty List<@Valid LineRequest> lines) {

    }

    public record LineRequest(@NotNull UUID ledgerAccountId, @NotNull EntryDirection direction, @Positive long amountAtomic) {

    }

    public record LedgerAccountResponse(UUID id, UUID walletId, Instant createdAt) {

        static LedgerAccountResponse from(LedgerAccount account) {
            return new LedgerAccountResponse(account.id().value(), account.walletId().value(), account.createdAt());
        }
    }

    public record BalanceResponse(UUID walletId, String assetCode, long balanceAtomic) {

    }

    public record JournalResponse(UUID id, String referenceType, String referenceId, String status, Instant createdAt,
            Instant postedAt, UUID reversalOfId, List<LedgerLineResponse> lines) {

        static JournalResponse from(JournalEntry journal) {
            return new JournalResponse(journal.id().value(), journal.referenceType(), journal.referenceId(), journal.status().name(),
                    journal.createdAt(), journal.postedAt(), journal.reversalOfId() == null ? null : journal.reversalOfId().value(),
                    journal.entries().stream().map(LedgerLineResponse::from).toList());
        }
    }

    public record LedgerLineResponse(UUID id, UUID ledgerAccountId, EntryDirection direction, long amountAtomic, Instant createdAt) {

        static LedgerLineResponse from(LedgerEntry entry) {
            return new LedgerLineResponse(entry.id().value(), entry.ledgerAccountId().value(), entry.direction(), entry.amountAtomic(), entry.createdAt());
        }
    }
}
