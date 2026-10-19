-- Allow nullable unit for products
ALTER TABLE products ALTER COLUMN unit DROP NOT NULL;

-- Add deleted_at column for soft deletes
ALTER TABLE products ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ DEFAULT NULL;

-- Add image_url column for product image/thumbnail
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url VARCHAR(500) DEFAULT NULL;

-- Index for filtering active products by workspace
CREATE INDEX IF NOT EXISTS idx_products_workspace_deleted
    ON products (workspace_id, deleted_at);
