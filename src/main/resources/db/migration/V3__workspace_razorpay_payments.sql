-- 1. Table for Workspace Payment Gateway Settings (encrypted credentials per workspace)
CREATE TABLE workspace_payment_settings
(
    id                       UUID                     NOT NULL,
    workspace_id             UUID                     NOT NULL,
    provider                 VARCHAR(50)              NOT NULL, -- 'RAZORPAY'
    key_id                   VARCHAR(255)             NOT NULL,
    key_secret_encrypted     VARCHAR(1000)            NOT NULL,
    webhook_secret_encrypted VARCHAR(1000),
    is_enabled               BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_workspace_payment_settings PRIMARY KEY (id),
    CONSTRAINT uk_workspace_payment_settings_workspace_provider UNIQUE (workspace_id, provider),
    CONSTRAINT fk_workspace_payment_settings_workspace FOREIGN KEY (workspace_id)
        REFERENCES workspaces (id) ON DELETE CASCADE
);

CREATE INDEX idx_workspace_payment_settings_workspace 
    ON workspace_payment_settings (workspace_id);

-- 2. Enhance payments table
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS workspace_id UUID,
    ADD COLUMN IF NOT EXISTS order_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS signature VARCHAR(255),
    ADD COLUMN IF NOT EXISTS currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50),
    ADD COLUMN IF NOT EXISTS error_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS error_description TEXT,
    ADD COLUMN IF NOT EXISTS notes TEXT;

ALTER TABLE payments
    ADD CONSTRAINT fk_payment_workspace FOREIGN KEY (workspace_id)
        REFERENCES workspaces (id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_order_id ON payments (order_id) WHERE order_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments (status);
CREATE INDEX IF NOT EXISTS idx_payments_workspace_id ON payments (workspace_id);

-- 3. Enhance invoice_access_tokens for public lookup
ALTER TABLE invoice_access_tokens
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_accessed_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_invoice_access_token_lookup 
    ON invoice_access_tokens (token_hash, revoked, expires_at);
