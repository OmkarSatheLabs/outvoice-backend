-- Add address, created_at, updated_at, deleted_at to customers table
ALTER TABLE customers ADD COLUMN IF NOT EXISTS address TEXT DEFAULT NULL;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE customers ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ DEFAULT NULL;

-- Index for filtering active customers by workspace
CREATE INDEX IF NOT EXISTS idx_customers_workspace_deleted
    ON customers (workspace_id, deleted_at);

-- Add customer snapshot fields to invoices table
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS customer_name VARCHAR(255) DEFAULT NULL;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS customer_email VARCHAR(255) DEFAULT NULL;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(50) DEFAULT NULL;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS customer_address TEXT DEFAULT NULL;

-- Backfill customer snapshot for existing invoices
UPDATE invoices i
SET customer_name = c.full_name,
    customer_email = c.email,
    customer_phone = TRIM(CONCAT(COALESCE(pc.code, ''), ' ', COALESCE(c.mobile, ''))),
    customer_address = c.address
FROM customers c
LEFT JOIN phone_codes pc ON c.phone_code_id = pc.id
WHERE i.customer_id = c.id
  AND i.customer_name IS NULL;
