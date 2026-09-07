-- CREATE TABLE currencies (
--     id             UUID         PRIMARY KEY,
--     code           VARCHAR(3)   NOT NULL UNIQUE,    -- ISO 4217 e.g. INR, USD, EUR
--     name           VARCHAR(100) NOT NULL,           -- e.g. Indian Rupee
--     symbol         VARCHAR(10)  NOT NULL,           -- e.g. ₹, $, €
--     decimal_places SMALLINT     NOT NULL DEFAULT 2, -- 0 for JPY, 3 for KWD
--     is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
--     created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
--     updated_at     TIMESTAMPTZ    DEFAULT null,
--     deleted_at     TIMESTAMPTZ    DEFAULT null
-- );

-- CREATE TABLE countries (
--     id                  UUID         PRIMARY KEY,
--     name                VARCHAR(100) NOT NULL,
--     iso_code_2          VARCHAR(2)   NOT NULL UNIQUE,  -- e.g. IN, US, GB
--     iso_code_3          VARCHAR(3)   NOT NULL UNIQUE,  -- e.g. IND, USA, GBR
--     default_currency_id UUID         REFERENCES currencies,
--     is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
--     created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
--     updated_at          TIMESTAMPTZ    DEFAULT null,
--     deleted_at          TIMESTAMPTZ    DEFAULT null
-- );

CREATE TABLE phone_codes
(
    id         UUID PRIMARY KEY,
    code       VARCHAR(10) NOT NULL UNIQUE, -- e.g. +91, +1, +44
    is_active  BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ          DEFAULT null,
    deleted_at TIMESTAMPTZ          DEFAULT null
);

-- CREATE TABLE country_phone_codes (
--     id            UUID        PRIMARY KEY,
--     country_id    UUID        NOT NULL REFERENCES countries,
--     phone_code_id UUID        NOT NULL REFERENCES phone_codes,
--     is_primary    BOOLEAN     NOT NULL DEFAULT FALSE,
--     created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
--     updated_at    TIMESTAMPTZ DEFAULT null,
--     deleted_at    TIMESTAMPTZ DEFAULT null,
--
--     CONSTRAINT uq_country_phone_code UNIQUE (country_id, phone_code_id)
-- );

-- CREATE UNIQUE INDEX uq_country_primary_phone_code
--     ON country_phone_codes (country_id)
--     WHERE is_primary = TRUE;

CREATE TABLE users
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) UNIQUE,
    phone_code_id UUID REFERENCES phone_codes,
    mobile        VARCHAR(20),
    full_name     VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL
--     is_email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
--     is_mobile_verified BOOLEAN      NOT NULL DEFAULT FALSE,
--     country_id         UUID         REFERENCES countries,
--     is_placeholder     BOOLEAN      NOT NULL DEFAULT FALSE,
--     is_active          BOOLEAN      NOT NULL DEFAULT TRUE,
--     invited_at         TIMESTAMPTZ,
--     invited_by         UUID         REFERENCES users,
--     created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
--     updated_at         TIMESTAMPTZ  DEFAULT null,
--     deleted_at         TIMESTAMPTZ  DEFAULT null,
--
--     UNIQUE(mobile, phone_code_id),
--
--     CHECK (
--         email IS NOT NULL
--             OR (phone_code_id IS NOT NULL AND mobile IS NOT NULL)
--     )
);

CREATE TABLE workspaces
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL
--     status                  VARCHAR      NOT NULL DEFAULT 'ACTIVE',
--     currency_id             UUID         REFERENCES currencies,
--     country_id              UUID         REFERENCES countries,
--     invoice_number_prefix   VARCHAR(20),
--     next_invoice_sequence   BIGINT       NOT NULL DEFAULT 1,
--
--     -- Audit
--     created_by              UUID         REFERENCES users,
--     created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
--     updated_by              UUID         REFERENCES users,
--     updated_at              TIMESTAMPTZ  DEFAULT null,
--     deleted_by              UUID         REFERENCES users,
--     deleted_at              TIMESTAMPTZ  DEFAULT null,
--
--     is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
--     is_deleted              BOOLEAN      NOT NULL DEFAULT FALSE,
--
--     CONSTRAINT uk_workspace_slug UNIQUE (slug),
--
--     CONSTRAINT chk_workspace_type
--         CHECK (type IN ('STANDARD', 'PLATFORM_ADMIN', 'SHADOW')),
--
--     CONSTRAINT chk_next_invoice_sequence_positive
--         CHECK (next_invoice_sequence > 0)
--     country_id              UUID         REFERENCES countries,
--     currency_id         UUID         REFERENCES currencies,
--     tax_compliance_name VARCHAR(255) NOT NULL,
--     pan_number          VARCHAR(20),
--     gst_number          VARCHAR(20),
--     tan_number          VARCHAR(20),
--     status              VARCHAR      NOT NULL DEFAULT 'ACTIVE',
--     is_placeholder      BOOLEAN      NOT NULL DEFAULT FALSE,
--     is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
--     created_by          UUID         REFERENCES users,
--     created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
--     updated_at          TIMESTAMP    DEFAULT null,
--     deleted_at          TIMESTAMPTZ    DEFAULT null
);

CREATE TABLE workspace_members
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces (id),
    user_id      UUID NOT NULL REFERENCES users (id),

    CONSTRAINT uk_workspace_member
        UNIQUE (workspace_id, user_id)
);

CREATE INDEX idx_workspace_members_user_id
    ON workspace_members (user_id);

-- CREATE INDEX idx_workspaces_type
--     ON workspaces (type)
--     WHERE is_deleted = FALSE;
--
-- CREATE INDEX idx_workspaces_claimed
--     ON workspaces (is_claimed)
--     WHERE is_deleted = FALSE AND is_claimed = FALSE;


-- CREATE TABLE user_workspaces (
--     id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
--     user_id              UUID        NOT NULL REFERENCES users,
--     workspace_id         UUID        NOT NULL REFERENCES workspaces,
--     role                 VARCHAR(20) CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER')),
--     is_default_workspace BOOLEAN     NOT NULL DEFAULT false,
--     invited_by           UUID        REFERENCES users ON DELETE RESTRICT,
--     joined_at            TIMESTAMPTZ,
--     status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INVITED', 'SUSPENDED', 'DEACTIVATED', 'REMOVED')),
--
--     UNIQUE (user_id, workspace_id)
-- );

-- CREATE UNIQUE INDEX one_default_workspace_per_user
--     ON user_workspaces (user_id)
--     WHERE is_default_workspace = true;
--
-- CREATE INDEX idx_user_workspaces_workspace_id ON user_workspaces (workspace_id);

CREATE TABLE customers
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- The biller — whose address book this customer belongs to
    workspace_id  UUID         NOT NULL REFERENCES workspaces (id),
    email         VARCHAR(255),
    phone_code_id UUID REFERENCES phone_codes,
    mobile        VARCHAR(20),
    full_name     VARCHAR(255) NOT NULL

--     display_name       VARCHAR(255) NOT NULL,
--     email              VARCHAR(255),
--     mobile             VARCHAR(20),
--
--     -- Populated once identity resolution matches this customer to a
--     -- real platform workspace (i.e. they've signed up). Null = shadow/unclaimed.
--     linked_workspace_id UUID REFERENCES workspaces(id),
--
--     default_currency    VARCHAR(3) NOT NULL,
--
--     -- Audit (from Auditable base class)
--     created_by          UUID,
--     created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
--     updated_by          UUID,
--     updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
--
--     -- Soft delete
--     is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
--
--     CONSTRAINT uk_workspace_customer_email
--     UNIQUE (workspace_id, email),
--
--     CONSTRAINT chk_customer_has_identity
--     CHECK (email IS NOT NULL OR mobile IS NOT NULL)
);

-- CREATE INDEX idx_workspace_customers_workspace_id
--     ON workspace_customers (workspace_id)
--     WHERE is_deleted = FALSE;
--
-- CREATE INDEX idx_workspace_customers_linked_workspace_id
--     ON workspace_customers (linked_workspace_id)
--     WHERE linked_workspace_id IS NOT NULL;
--
-- -- Supports typeahead search on name/email/mobile
-- CREATE INDEX idx_workspace_customers_search
--     ON workspace_customers (workspace_id, display_name, email, mobile)
--     WHERE is_deleted = FALSE;

CREATE TABLE invoices
(
    id           UUID PRIMARY KEY        DEFAULT gen_random_uuid(),

    -- Biller (always a Workspace)
    workspace_id UUID           NOT NULL REFERENCES workspaces (id),
    customer_id  UUID           NOT NULL REFERENCES customers (id),
    total        NUMERIC(18, 2) NOT NULL DEFAULT 0,
    tax          NUMERIC(18, 2) NOT NULL DEFAULT 0,
    discount     NUMERIC(18, 2) NOT NULL DEFAULT 0,
    net_total    NUMERIC(18, 2) NOT NULL DEFAULT 0,
    issue_date   DATE,
    due_date     DATE

    -- Billee (resolved through the customer record)
--     customer_id             UUID NOT NULL REFERENCES customers(id),
--
--     invoice_number          VARCHAR(50) NOT NULL,
--
--     status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
--
--     issue_date              DATE NOT NULL,
--     due_date                DATE,
--
--     -- CurrencyEntity, locked at finalization
--     currency                VARCHAR(3) NOT NULL,
--     fx_rate_to_base         NUMERIC(18, 8),
--     base_currency_amount    NUMERIC(18, 2),
--     fx_rate_captured_at     TIMESTAMPTZ,
--
--     -- Amounts
--     subtotal                NUMERIC(18, 2) NOT NULL DEFAULT 0,
--     tax_amount              NUMERIC(18, 2) NOT NULL DEFAULT 0,
--     discount_amount         NUMERIC(18, 2) NOT NULL DEFAULT 0,
--     total_amount            NUMERIC(18, 2) NOT NULL DEFAULT 0,
--     amount_paid             NUMERIC(18, 2) NOT NULL DEFAULT 0,
--
--     -- GST / compliance
--     gstin                   VARCHAR(15),
--     place_of_supply         VARCHAR(100),
--     is_gst_applicable       BOOLEAN NOT NULL DEFAULT FALSE,
--
--     -- Misc
--     notes                   TEXT,
--     terms                   TEXT,
--     finalized_at            TIMESTAMPTZ,
--
--     -- Audit (from Auditable base class)
--     created_by              UUID,
--     created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
--     updated_by              UUID,
--     updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
--
--     -- Soft delete
--     is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
--
--     CONSTRAINT uk_workspace_invoice_number
--         UNIQUE (workspace_id, invoice_number),
--
--     CONSTRAINT chk_invoice_status
--         CHECK (status IN (
--               'DRAFT', 'FINALIZED', 'SENT', 'PARTIALLY_PAID',
--               'PAID', 'OVERDUE', 'CANCELLED', 'VOID'
--             )),
--
--     CONSTRAINT chk_invoice_amounts_non_negative
--         CHECK (
--             subtotal >= 0 AND tax_amount >= 0 AND
--             discount_amount >= 0 AND total_amount >= 0 AND
--             amount_paid >= 0
--             ),
--
--     CONSTRAINT chk_amount_paid_not_exceeding_total
--         CHECK (amount_paid <= total_amount)

--     invoice_number             VARCHAR(50)    NOT NULL,
--     amount                     NUMERIC(15, 2) NOT NULL,
--     currency_code              VARCHAR(3)     NOT NULL,
--     status                     VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
--     issue_date                 DATE           NOT NULL,
--     due_date                   DATE           NOT NULL,
--     last_reminder_sent_at      TIMESTAMP,
--     next_reminder_scheduled_at DATE,
--     tax_rate                   NUMERIC(5, 2)  NOT NULL DEFAULT 0.00,
--     created_at                 TIMESTAMP      NOT NULL DEFAULT NOW(),
--     created_by                 UUID           NOT NULL REFERENCES users,
--     updated_at                 TIMESTAMP      DEFAULT null,
--     updated_by                 UUID           REFERENCES users,
--     deleted_at                 TIMESTAMP      DEFAULT null,
--     deleted_by                 UUID           DEFAULT null REFERENCES users,
--
--     CONSTRAINT uq_workspace_invoice_number UNIQUE (workspace_id, invoice_number)
);

CREATE TABLE products
(
    id           UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    workspace_id UUID           NOT NULL REFERENCES workspaces (id),
    name         VARCHAR(255)   NOT NULL,
    stock        INT            NOT NULL DEFAULT 0,
    unit         VARCHAR(20)    NOT NULL,
    price        NUMERIC(18, 2) NOT NULL DEFAULT 0
);

CREATE TABLE items
(
    id            UUID PRIMARY KEY,
    invoice_id    UUID           NOT NULL REFERENCES invoices (id),
    product_id    UUID REFERENCES products (id),
    product_name  VARCHAR(255)   NOT NULL,
    product_price NUMERIC(18, 2) NOT NULL DEFAULT 0,
    quantity      INT            NOT NULL,
    total         NUMERIC(18, 2) NOT NULL DEFAULT 0
);
-- Indexes for common access patterns
-- CREATE INDEX idx_workspace_invoices_workspace_id
--     ON workspace_invoices (workspace_id);
--
-- CREATE INDEX idx_workspace_invoices_customer_id
--     ON workspace_invoices (customer_id);
--
-- CREATE INDEX idx_workspace_invoices_status
--     ON workspace_invoices (workspace_id, status)
--     WHERE is_deleted = FALSE;
--
-- CREATE INDEX idx_workspace_invoices_due_date
--     ON workspace_invoices (workspace_id, due_date)
--     WHERE is_deleted = FALSE AND status NOT IN ('PAID', 'CANCELLED', 'VOID');
--
-- CREATE INDEX idx_workspace_invoices_issue_date
--     ON workspace_invoices (workspace_id, issue_date);

-- CREATE TABLE workspace_invoice_line_items (
--     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--
--     invoice_id UUID NOT NULL REFERENCES workspace_invoices(id) ON DELETE CASCADE,
--
--     sort_order INTEGER NOT NULL DEFAULT 0,
--
--     description TEXT NOT NULL,
--
--     quantity NUMERIC(18, 4) NOT NULL DEFAULT 1,
--     unit_price NUMERIC(18, 2) NOT NULL,
--     line_subtotal NUMERIC(18, 2) NOT NULL,
--
--     tax_rate NUMERIC(5, 2),
--     tax_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
--     discount_amount NUMERIC(18, 2) NOT NULL DEFAULT 0,
--     line_total NUMERIC(18, 2) NOT NULL,
--
--     hsn_sac_code VARCHAR(20),
--
--     -- Audit
--     created_by UUID,
--     created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
--     updated_by UUID,
--     updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
--
--     is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
--
--     CONSTRAINT chk_line_item_amounts_non_negative
--         CHECK (
--             quantity > 0 AND unit_price >= 0 AND
--             line_subtotal >= 0 AND tax_amount >= 0 AND
--             discount_amount >= 0 AND line_total >= 0
--         )
-- );
--
-- CREATE INDEX idx_workspace_invoice_line_items_invoice_id
--     ON workspace_invoice_line_items (invoice_id, sort_order)
--     WHERE is_deleted = FALSE;

-- CREATE TABLE invoice_line_items (
--     id          UUID           PRIMARY KEY,
--     invoice_id  UUID           NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
--     description VARCHAR(255)   NOT NULL,
--     quantity    INT            NOT NULL DEFAULT 1,
--     unit_price  NUMERIC(15, 2) NOT NULL,
--     amount      NUMERIC(15, 2) NOT NULL,
--     created_at  TIMESTAMP      NOT NULL DEFAULT NOW()
-- );

-- CREATE TABLE teams (
--     id           UUID         PRIMARY KEY,
--     workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
--     name         VARCHAR(100) NOT NULL,
--     description  VARCHAR(255),
--     created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
--     created_by   UUID         NOT NULL REFERENCES users,
--     updated_at   TIMESTAMP    DEFAULT null,
--     updated_by   UUID         REFERENCES users,
--     deleted_at   TIMESTAMP    DEFAULT null,
--     deleted_by   UUID         REFERENCES users
-- );
--
-- CREATE TABLE roles (
--     id           UUID         PRIMARY KEY,
--     workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
--     name         VARCHAR(100) NOT NULL,
--     description  VARCHAR(255),
--     created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
--     created_by   UUID         NOT NULL REFERENCES users,
--     updated_at   TIMESTAMP    DEFAULT null,
--     updated_by   UUID         REFERENCES users,
--     deleted_at   TIMESTAMP    DEFAULT null,
--     deleted_by   UUID         REFERENCES users
-- );
--
-- CREATE TABLE user_workspace_teams (
--     user_workspace_id UUID NOT NULL REFERENCES user_workspaces(id) ON DELETE CASCADE,
--     team_id           UUID NOT NULL REFERENCES teams(id)           ON DELETE CASCADE,
--     PRIMARY KEY (user_workspace_id, team_id)
-- );
--
-- CREATE TABLE user_workspace_roles (
--     user_workspace_id UUID NOT NULL REFERENCES user_workspaces(id) ON DELETE CASCADE,
--     role_id           UUID NOT NULL REFERENCES roles(id)    ON DELETE CASCADE,
--     PRIMARY KEY (user_workspace_id, role_id)
-- );
--
-- CREATE TABLE invites (
--     id            UUID        PRIMARY KEY,
--     workspace_id  UUID        NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
--     user_id       UUID        REFERENCES users(id),
--     email         VARCHAR(255),
--     phone_code_id UUID         REFERENCES phone_codes(id),
--     mobile        VARCHAR(20),
--     invited_by    UUID        REFERENCES users(id),
--     created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
--     created_by    UUID        NOT NULL REFERENCES users(id),
--     updated_at    TIMESTAMPTZ,
--     updated_by    UUID        REFERENCES users(id)
-- );

-- CREATE TABLE user_workspace_invitations (
--     id            UUID         PRIMARY KEY,
--     workspace_id  UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
--     user_id       UUID         NULL REFERENCES users(id)          ON DELETE CASCADE,
--     email         VARCHAR(255),
--     phone_code_id UUID         REFERENCES phone_codes(id),
--     mobile        VARCHAR(20),
--     role          VARCHAR      NOT NULL,
--     invited_by    UUID         REFERENCES users(id),
--     token         VARCHAR(255) NOT NULL UNIQUE,
--     status        VARCHAR      NOT NULL DEFAULT 'PENDING',
--     created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
--     expires_at    TIMESTAMP    NOT NULL
-- );
--
-- CREATE TABLE workspace_invitation_teams (
--     invitation_id UUID NOT NULL REFERENCES user_workspace_invitations(id) ON DELETE CASCADE,
--     team_id       UUID NOT NULL REFERENCES teams(id)                      ON DELETE CASCADE,
--     PRIMARY KEY (invitation_id, team_id)
-- );
--
-- CREATE TABLE workspace_invitation_roles (
--     invitation_id UUID NOT NULL REFERENCES user_workspace_invitations(id) ON DELETE CASCADE,
--     role_id       UUID NOT NULL REFERENCES custom_roles(id)               ON DELETE CASCADE,
--     PRIMARY KEY (invitation_id, role_id)
-- );

CREATE TABLE mail_outbox
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    recipient        VARCHAR(255) NOT NULL,
    type             VARCHAR(50)  NOT NULL,
    variables        JSONB        NOT NULL,
    attachment_paths JSONB,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    attempts         INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at  TIMESTAMPTZ,
    sent_at          TIMESTAMPTZ,
    last_error       VARCHAR(2000)
);

CREATE INDEX idx_mail_outbox_pending
    ON mail_outbox (status, next_attempt_at);

CREATE TABLE sms_outbox
(
    id                  UUID PRIMARY KEY,
    recipient           VARCHAR(255) NOT NULL,
    message             TEXT         NOT NULL,
    status              VARCHAR(50)  NOT NULL,
    attempts            INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at     TIMESTAMP WITH TIME ZONE,
    sent_at             TIMESTAMP WITH TIME ZONE,
    last_error          TEXT,
    provider_message_id VARCHAR(255),
    provider_batch_id   VARCHAR(255),
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_sms_outbox_pending
    ON sms_outbox (status, next_attempt_at, created_at);

CREATE TABLE password_reset_tokens
(
    id         UUID PRIMARY KEY,

    user_id    UUID                     NOT NULL,

    token_hash VARCHAR(128)             NOT NULL UNIQUE,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,

    used_at    TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_user_id
    ON password_reset_tokens (user_id);

CREATE INDEX idx_password_reset_tokens_expires_at
    ON password_reset_tokens (expires_at);

CREATE TABLE invoice_access_tokens
(
    id         UUID         NOT NULL,
    invoice_id UUID         NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_invoice_access_token
        PRIMARY KEY (id),

    CONSTRAINT uk_invoice_access_token_token_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_invoice_access_token_invoice
        FOREIGN KEY (invoice_id)
            REFERENCES invoices (id)
);

CREATE INDEX idx_invoice_access_token_invoice_id
    ON invoice_access_tokens (invoice_id);

CREATE TABLE payments
(
    id                  UUID                     NOT NULL,
    invoice_id          UUID                     NOT NULL,
    amount              NUMERIC(19, 2)           NOT NULL,
    status              VARCHAR(50)              NOT NULL,
    provider            VARCHAR(100),
    provider_payment_id VARCHAR(255),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at        TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_payment
        PRIMARY KEY (id),

    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id)
            REFERENCES invoices (id)
);

CREATE INDEX idx_payment_invoice_id
    ON payments (invoice_id);

CREATE INDEX idx_payment_provider_payment_id
    ON payments (provider_payment_id);

CREATE TABLE pdf_processor
(
    id           UUID                     NOT NULL,
    type         VARCHAR(50)              NOT NULL,
    data         JSONB,
    status       VARCHAR(50)              NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    url          VARCHAR(2048),
    error        TEXT,

    CONSTRAINT pk_pdf_processor
        PRIMARY KEY (id)
);

CREATE INDEX idx_pdf_processor_status
    ON pdf_processor (status);

CREATE INDEX idx_pdf_processor_status_created_at
    ON pdf_processor (status, created_at);