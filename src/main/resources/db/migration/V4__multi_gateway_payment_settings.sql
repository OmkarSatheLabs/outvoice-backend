-- 1. Relax NOT NULL constraints on key_id and key_secret_encrypted (only required for Razorpay)
ALTER TABLE workspace_payment_settings
    ALTER COLUMN key_id DROP NOT NULL,
    ALTER COLUMN key_secret_encrypted DROP NOT NULL;

-- 2. Add multi-gateway columns and primary flag
ALTER TABLE workspace_payment_settings
    ADD COLUMN IF NOT EXISTS is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS upi_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payee_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS bank_account_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS bank_account_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bank_ifsc_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS bank_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS bank_branch VARCHAR(150),
    ADD COLUMN IF NOT EXISTS bank_swift_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS instructions TEXT;

-- 3. Enforce that only ONE payment method per workspace can be primary at any time
CREATE UNIQUE INDEX IF NOT EXISTS uk_workspace_payment_primary 
    ON workspace_payment_settings (workspace_id) 
    WHERE is_primary = TRUE;

-- 4. Add index for quick lookup of enabled methods
CREATE INDEX IF NOT EXISTS idx_workspace_payment_enabled 
    ON workspace_payment_settings (workspace_id, is_enabled);
