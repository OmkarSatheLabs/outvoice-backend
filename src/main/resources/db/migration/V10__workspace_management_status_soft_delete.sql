-- Add status, soft delete timestamp, and audit timestamps to workspaces
ALTER TABLE workspaces
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NULL;

-- Indexes for efficient status filtering and soft-delete queries
CREATE INDEX IF NOT EXISTS idx_workspaces_status ON workspaces (status);
CREATE INDEX IF NOT EXISTS idx_workspaces_deleted_at ON workspaces (deleted_at);
