-- Add image_key column to products table for canonical object key storage
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_key VARCHAR(500) DEFAULT NULL;

-- Create product_images table to support multi-image metadata and ordering
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces (id),
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    object_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id);
CREATE INDEX idx_product_images_workspace_id ON product_images (workspace_id);
