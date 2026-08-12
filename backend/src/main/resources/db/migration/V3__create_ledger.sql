CREATE TABLE assets (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    decimal_places SMALLINT NOT NULL,

    CONSTRAINT assets_code_format_valid
        CHECK (code ~ '^[A-Z0-9]{2,10}$'),

    CONSTRAINT assets_name_not_blank
        CHECK (length(trim(name)) > 0),

    CONSTRAINT assets_decimal_places_valid
        CHECK (decimal_places BETWEEN 0 AND 18)
);

CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY,
    account_type VARCHAR(20) NOT NULL,
    wallet_id UUID REFERENCES wallets(id),
    system_account_code VARCHAR(60),
    asset_code VARCHAR(10) NOT NULL REFERENCES assets(code),
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT ledger_accounts_type_valid
        CHECK (account_type IN ('WALLET', 'SYSTEM')),

    CONSTRAINT ledger_accounts_owner_valid
        CHECK (
            (
                account_type = 'WALLET'
                AND wallet_id IS NOT NULL
                AND system_account_code IS NULL
            )
            OR
            (
                account_type = 'SYSTEM'
                AND wallet_id IS NULL
                AND system_account_code IS NOT NULL
                AND length(trim(system_account_code)) > 0
            )
        ),

    CONSTRAINT ledger_accounts_system_code_format_valid
        CHECK (
            system_account_code IS NULL
            OR system_account_code ~ '^[A-Z][A-Z0-9_]{1,59}$'
        ),

    CONSTRAINT ledger_accounts_wallet_asset_unique
        UNIQUE (wallet_id, asset_code),

    CONSTRAINT ledger_accounts_system_asset_unique
        UNIQUE (system_account_code, asset_code)
);

CREATE INDEX ledger_accounts_asset_code_idx
    ON ledger_accounts (asset_code);

CREATE TABLE journal_entries (
    id UUID PRIMARY KEY,
    reference_type VARCHAR(40) NOT NULL,
    reference_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    posted_at TIMESTAMPTZ,
    reversal_of_id UUID UNIQUE REFERENCES journal_entries(id),

    CONSTRAINT journal_entries_reference_type_not_blank
        CHECK (length(trim(reference_type)) > 0),

    CONSTRAINT journal_entries_reference_id_not_blank
        CHECK (
            reference_id IS NULL
            OR length(trim(reference_id)) > 0
        ),

    CONSTRAINT journal_entries_status_valid
        CHECK (status IN ('DRAFT', 'POSTED', 'REVERSED')),

    CONSTRAINT journal_entries_posted_at_valid
        CHECK (
            (status = 'DRAFT' AND posted_at IS NULL)
            OR
            (status IN ('POSTED', 'REVERSED') AND posted_at IS NOT NULL)
        ),

    CONSTRAINT journal_entries_not_self_reversal
        CHECK (
            reversal_of_id IS NULL
            OR reversal_of_id <> id
        )
);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    journal_entry_id UUID NOT NULL REFERENCES journal_entries(id),
    ledger_account_id UUID NOT NULL REFERENCES ledger_accounts(id),
    direction VARCHAR(10) NOT NULL,
    amount_atomic NUMERIC(78, 0) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT ledger_entries_direction_valid
        CHECK (direction IN ('DEBIT', 'CREDIT')),

    CONSTRAINT ledger_entries_amount_positive
        CHECK (amount_atomic > 0)
);

CREATE INDEX ledger_entries_account_idx ON ledger_entries (ledger_account_id);
CREATE INDEX ledger_entries_journal_idx ON ledger_entries (journal_entry_id);