CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL UNIQUE REFERENCES wallets(id),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE journal_entries (
    id UUID PRIMARY KEY,
    reference_type VARCHAR(40) NOT NULL,
    reference_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    posted_at TIMESTAMPTZ,
    reversal_of_id UUID UNIQUE REFERENCES journal_entries(id),
    CONSTRAINT journal_entries_status_valid CHECK (status IN ('DRAFT', 'POSTED', 'REVERSED'))
);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    journal_entry_id UUID NOT NULL REFERENCES journal_entries(id),
    ledger_account_id UUID NOT NULL REFERENCES ledger_accounts(id),
    direction VARCHAR(10) NOT NULL,
    amount_atomic BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ledger_entries_direction_valid CHECK (direction IN ('DEBIT', 'CREDIT')),
    CONSTRAINT ledger_entries_amount_positive CHECK (amount_atomic > 0)
);

CREATE INDEX ledger_entries_account_idx ON ledger_entries(ledger_account_id);
CREATE INDEX ledger_entries_journal_idx ON ledger_entries(journal_entry_id);
