-- Add status and description to products
ALTER TABLE products ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE products ADD COLUMN IF NOT EXISTS description TEXT;

CREATE INDEX IF NOT EXISTS idx_products_workspace_status_deleted
    ON products (workspace_id, status, deleted_at);

-- Create storefronts table
CREATE TABLE IF NOT EXISTS storefronts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL UNIQUE REFERENCES workspaces(id) ON DELETE CASCADE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    url_strategy VARCHAR(50) NOT NULL DEFAULT 'PATH_BASED',
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    branding_config TEXT DEFAULT '{}',
    pages_config TEXT DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_storefronts_slug ON storefronts(slug);
CREATE INDEX IF NOT EXISTS idx_storefronts_workspace ON storefronts(workspace_id);

-- Create storefront_domains table for custom domain management
CREATE TABLE IF NOT EXISTS storefront_domains (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    storefront_id UUID NOT NULL REFERENCES storefronts(id) ON DELETE CASCADE,
    hostname VARCHAR(255) NOT NULL UNIQUE,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    verification_token VARCHAR(255) NOT NULL,
    verification_token_hash VARCHAR(255) NOT NULL,
    verified_at TIMESTAMPTZ DEFAULT NULL,
    tls_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    tls_provider_reference VARCHAR(255) DEFAULT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_storefront_domains_hostname ON storefront_domains(hostname);
CREATE INDEX IF NOT EXISTS idx_storefront_domains_storefront ON storefront_domains(storefront_id);
