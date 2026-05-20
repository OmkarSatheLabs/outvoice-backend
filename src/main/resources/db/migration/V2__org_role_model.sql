-- V2: Full schema rewrite for org/role model.
-- Drops V1 BIGSERIAL-based tables and recreates with UUID primary keys,
-- status fields, and the org_users, org_invitations, audit_logs, platform_users tables.

-- pgcrypto provides gen_random_uuid() on PG < 13
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Drop V1 tables (order respects FK: users references organizations)
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS organizations CASCADE;

-- ─── organizations ────────────────────────────────────────────────────────────
CREATE TABLE organizations (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    slug          VARCHAR(100) NOT NULL UNIQUE,
    owner_user_id UUID,                               -- denormalized; FK added after users
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ─── users ────────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name        VARCHAR(255),
    email            VARCHAR(255) UNIQUE,
    mobile_number    VARCHAR(20)  UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    email_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    mobile_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                         CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_or_mobile CHECK (email IS NOT NULL OR mobile_number IS NOT NULL)
);

-- Now add the FK from organizations.owner_user_id → users.id
ALTER TABLE organizations
    ADD CONSTRAINT fk_org_owner FOREIGN KEY (owner_user_id) REFERENCES users (id);

-- ─── org_users ────────────────────────────────────────────────────────────────
-- Core role/membership join entity.  Each row = one user's membership in one org.
CREATE TABLE org_users (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID        NOT NULL REFERENCES organizations (id),
    user_id     UUID        NOT NULL REFERENCES users (id),
    role        VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'SUPER_USER', 'MEMBER')),
    permissions TEXT,       -- JSON array of permission strings; NULL for OWNER/SUPER_USER
    invited_by  UUID        REFERENCES users (id),
    status      VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'INVITED', 'PENDING_APPROVAL', 'SUSPENDED')),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_org_user UNIQUE (org_id, user_id)
);

-- Business rule: exactly one ACTIVE OWNER per org at all times.
CREATE UNIQUE INDEX one_owner_per_org
    ON org_users (org_id)
    WHERE role = 'OWNER' AND status = 'ACTIVE';

-- ─── org_invitations ──────────────────────────────────────────────────────────
CREATE TABLE org_invitations (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id               UUID         NOT NULL REFERENCES organizations (id),
    invited_by           UUID         NOT NULL REFERENCES users (id),
    invitee_email        VARCHAR(255),
    invitee_mobile       VARCHAR(20),
    intended_role        VARCHAR(20)  NOT NULL CHECK (intended_role IN ('SUPER_USER', 'MEMBER')),
    intended_permissions TEXT,        -- JSON array
    approval_status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                             CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    approved_by          UUID         REFERENCES users (id),
    token                VARCHAR(255) NOT NULL UNIQUE,
    expires_at           TIMESTAMP    NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                             CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED')),
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ─── audit_logs ───────────────────────────────────────────────────────────────
CREATE TABLE audit_logs (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id         UUID         REFERENCES organizations (id),
    actor_user_id  UUID         NOT NULL REFERENCES users (id),
    action         VARCHAR(100) NOT NULL,
    target_user_id UUID         REFERENCES users (id),
    old_value      TEXT,        -- JSON
    new_value      TEXT,        -- JSON
    ip_address     VARCHAR(50),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ─── platform_users ───────────────────────────────────────────────────────────
-- Outvoice internal operators; completely separate from org users.
CREATE TABLE platform_users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL
                      CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'FINANCE', 'SUPPORT')),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);
