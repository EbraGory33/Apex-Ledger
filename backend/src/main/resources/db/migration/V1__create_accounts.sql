CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT accounts_display_name_not_blank
        CHECK (length(trim(display_name)) > 0),

    CONSTRAINT accounts_status_valid
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'))

);