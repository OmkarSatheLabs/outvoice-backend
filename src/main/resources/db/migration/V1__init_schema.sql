CREATE TABLE currencies (
    id             UUID         PRIMARY KEY,
    code           VARCHAR(3)   NOT NULL UNIQUE,    -- ISO 4217 e.g. INR, USD, EUR
    name           VARCHAR(100) NOT NULL,           -- e.g. Indian Rupee
    symbol         VARCHAR(10)  NOT NULL,           -- e.g. ₹, $, €
    decimal_places SMALLINT     NOT NULL DEFAULT 2, -- 0 for JPY, 3 for KWD
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMP    DEFAULT null
);

CREATE TABLE countries (
    id                  UUID         PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    iso_code_2          VARCHAR(2)   NOT NULL UNIQUE,  -- e.g. IN, US, GB
    iso_code_3          VARCHAR(3)   NOT NULL UNIQUE,  -- e.g. IND, USA, GBR
    default_currency_id UUID         REFERENCES currencies,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP    DEFAULT null
);

CREATE TABLE phone_codes (
    id         UUID        PRIMARY KEY,
    code       VARCHAR(10) NOT NULL UNIQUE,  -- e.g. +91, +1, +44
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP   DEFAULT null
);

CREATE TABLE country_phone_codes (
    id            UUID      PRIMARY KEY,
    country_id    UUID      NOT NULL REFERENCES countries,
    phone_code_id UUID      NOT NULL REFERENCES phone_codes,
    is_primary    BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_country_phone_code UNIQUE (country_id, phone_code_id)
);

CREATE UNIQUE INDEX uq_country_primary_phone_code
    ON country_phone_codes (country_id)
    WHERE is_primary = TRUE;

CREATE TABLE users (
    id                 UUID         PRIMARY KEY,
    email              VARCHAR(255) UNIQUE,
    phone_code_id      UUID         REFERENCES phone_codes,
    mobile             VARCHAR(20)  UNIQUE,
    full_name          VARCHAR(255) NOT NULL,
    password_hash      VARCHAR(255) NOT NULL,
    is_email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    is_mobile_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    country_id         UUID         REFERENCES countries,
    is_placeholder     BOOLEAN      NOT NULL DEFAULT FALSE,
    invited_at         TIMESTAMP,
    invited_by         UUID         REFERENCES users,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at         TIMESTAMP    DEFAULT null
);

CREATE TABLE workspaces (
    id                  UUID         PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    country_id          UUID         REFERENCES countries,
    currency_id         UUID         REFERENCES currencies,
    tax_compliance_name VARCHAR(255) NOT NULL,
--     pan_number          VARCHAR(20),
--     gst_number          VARCHAR(20),
--     tan_number          VARCHAR(20),
    status              VARCHAR      NOT NULL DEFAULT 'ACTIVE',
    is_placeholder      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by          UUID         REFERENCES users,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP    DEFAULT null
);

CREATE TABLE user_workspaces (
    id                   UUID      PRIMARY KEY,
    user_id              UUID      REFERENCES users,
    workspace_id         UUID      REFERENCES workspaces,
    role                 VARCHAR   NOT NULL,
    is_default_workspace BOOLEAN   DEFAULT false,
    invited_by           UUID      REFERENCES users,
    joined_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    status               VARCHAR
);

CREATE TABLE customers (
    id                  UUID         PRIMARY KEY,
    customer_name       VARCHAR(255),
    company_name        VARCHAR(255),
    email               VARCHAR(255),
    phone_code_id       UUID         REFERENCES phone_codes,
    mobile              VARCHAR(20)  UNIQUE,
    user_id             UUID         REFERENCES users,
    workspace_id        UUID         REFERENCES workspaces,
    workspace_slug      VARCHAR(255),
    country_id          UUID         REFERENCES countries,
    currency_id         UUID         REFERENCES currencies,
--     billing_address     TEXT,
--     tax_number          VARCHAR(50),
--     client_workspace_id UUID         REFERENCES workspaces,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP    DEFAULT null
);

CREATE TABLE invoices (
    id                         UUID           PRIMARY KEY,
    invoice_number             VARCHAR(50)    NOT NULL,
    workspace_id               UUID           REFERENCES workspaces,
    customer_id                UUID           REFERENCES customers,
    amount                     NUMERIC(15, 2) NOT NULL,
    currency_code              VARCHAR(3)     NOT NULL,
    status                     VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    issue_date                 DATE           NOT NULL,
    due_date                   DATE           NOT NULL,
    last_reminder_sent_at      TIMESTAMP,
    next_reminder_scheduled_at DATE,
    tax_rate                   NUMERIC(5, 2)  NOT NULL DEFAULT 0.00,
    created_at                 TIMESTAMP      NOT NULL DEFAULT NOW(),
    created_by                 UUID           NOT NULL REFERENCES users,
    updated_at                 TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_by                 UUID           NOT NULL REFERENCES users,
    deleted_at                 TIMESTAMP      DEFAULT null,
    deleted_by                 UUID           DEFAULT null REFERENCES users,

    CONSTRAINT uq_workspace_invoice_number UNIQUE (workspace_id, invoice_number)
);

CREATE TABLE invoice_line_items (
    id          UUID           PRIMARY KEY,
    invoice_id  UUID           NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(255)   NOT NULL,
    quantity    INT            NOT NULL DEFAULT 1,
    unit_price  NUMERIC(15, 2) NOT NULL,
    amount      NUMERIC(15, 2) NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE teams (
    id           UUID         PRIMARY KEY,
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE custom_roles (
    id           UUID         PRIMARY KEY,
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE user_workspace_teams (
    user_workspace_id UUID NOT NULL REFERENCES user_workspaces(id) ON DELETE CASCADE,
    team_id           UUID NOT NULL REFERENCES teams(id)           ON DELETE CASCADE,
    PRIMARY KEY (user_workspace_id, team_id)
);

CREATE TABLE user_workspace_roles (
    user_workspace_id UUID NOT NULL REFERENCES user_workspaces(id) ON DELETE CASCADE,
    role_id           UUID NOT NULL REFERENCES custom_roles(id)    ON DELETE CASCADE,
    PRIMARY KEY (user_workspace_id, role_id)
);

CREATE TABLE user_workspace_invitations (
    id            UUID         PRIMARY KEY,
    workspace_id  UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id       UUID         NULL REFERENCES users(id)          ON DELETE CASCADE,
    email         VARCHAR(255),
    phone_code_id UUID         REFERENCES phone_codes(id),
    mobile        VARCHAR(20),
    role          VARCHAR      NOT NULL,
    invited_by    UUID         REFERENCES users(id),
    token         VARCHAR(255) NOT NULL UNIQUE,
    status        VARCHAR      NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at    TIMESTAMP    NOT NULL
);

CREATE TABLE workspace_invitation_teams (
    invitation_id UUID NOT NULL REFERENCES user_workspace_invitations(id) ON DELETE CASCADE,
    team_id       UUID NOT NULL REFERENCES teams(id)                      ON DELETE CASCADE,
    PRIMARY KEY (invitation_id, team_id)
);

CREATE TABLE workspace_invitation_roles (
    invitation_id UUID NOT NULL REFERENCES user_workspace_invitations(id) ON DELETE CASCADE,
    role_id       UUID NOT NULL REFERENCES custom_roles(id)               ON DELETE CASCADE,
    PRIMARY KEY (invitation_id, role_id)
);
