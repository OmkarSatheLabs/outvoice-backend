CREATE TABLE customers (
    id            UUID PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    workspace_id  UUID NOT NULL REFERENCES workspaces(id),
    phone_code_id UUID REFERENCES phone_codes(id),
    mobile        VARCHAR(20),
    company_name  VARCHAR(255),
    billing_address TEXT,
    tax_number    VARCHAR(50),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
