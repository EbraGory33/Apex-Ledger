CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    label VARCHAR(80) NOT NULL,
    asset_code VARCHAR(10) NOT NULL DEFAULT 'APX',
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT wallets_label_not_blank CHECK (length(trim(label)) > 0),
    CONSTRAINT wallets_asset_code_valid CHECK (asset_code = 'APX'),
    CONSTRAINT wallets_status_valid CHECK (status IN ('ACTIVE', 'FROZEN'))
);

CREATE UNIQUE INDEX wallets_account_label_unique
    ON wallets (account_id, lower(trim(label)));

CREATE INDEX wallets_account_id_idx ON wallets (account_id);
