-- 1. Clear legacy dummy audit records
DELETE FROM audit_log;

-- 2. Add workspace_id, action, actor_name, and actor_email columns
ALTER TABLE audit_log
    ADD COLUMN IF NOT EXISTS workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS action VARCHAR(50) NOT NULL DEFAULT 'UPDATE',
    ADD COLUMN IF NOT EXISTS actor_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS actor_email VARCHAR(200);

-- 3. Relax NOT NULL constraint on changed_by (allows system and webhook events)
ALTER TABLE audit_log
    ALTER COLUMN changed_by DROP NOT NULL;

-- 4. Set default value for changes JSONB column
ALTER TABLE audit_log
    ALTER COLUMN changes SET DEFAULT '{}'::jsonb;

-- 5. Indexes for fast lookup, pagination, and filtering
CREATE INDEX IF NOT EXISTS idx_audit_log_workspace_changed_at
    ON audit_log (workspace_id, changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_log_resource
    ON audit_log (workspace_id, resource_type, resource_id);

CREATE INDEX IF NOT EXISTS idx_audit_log_action
    ON audit_log (workspace_id, action);
