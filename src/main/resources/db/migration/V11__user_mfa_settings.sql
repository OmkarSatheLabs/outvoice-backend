-- V11: Multi-Factor Authentication (MFA) settings and challenges

ALTER TABLE users ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS mfa_channel VARCHAR(32) DEFAULT 'EMAIL';
ALTER TABLE users ADD COLUMN IF NOT EXISTS totp_secret VARCHAR(64);

CREATE TABLE IF NOT EXISTS user_mfa_challenges (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(128) NOT NULL UNIQUE,
    otp_hash VARCHAR(128),
    delivery_channel VARCHAR(32) NOT NULL,
    destination VARCHAR(255),
    attempts INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mfa_challenges_user_id ON user_mfa_challenges(user_id);
CREATE INDEX IF NOT EXISTS idx_mfa_challenges_session_token ON user_mfa_challenges(session_token);
CREATE INDEX IF NOT EXISTS idx_mfa_challenges_expires_at ON user_mfa_challenges(expires_at);
