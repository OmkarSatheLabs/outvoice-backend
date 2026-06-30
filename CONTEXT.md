We need to seed the platform organization and its owner user via a Flyway migration.

[//]: # (PLATFORM_ADMIN_PASSWORD=GWII4kj3tAXpwqXA;PLATFORM_ADMIN_EMAIL=omkar@omkarsathe.com)

## Context

- The `outvoice` org is the platform-controlling organization (slug = `outvoice`).
- Its users are treated as superusers on the platform.
- The seed data must use fixed UUIDs so re-runs are idempotent.

## What to implement

Create Flyway migration `V3__seed_platform_org.sql` in `src/main/resources/db/migration/`.

### 1. Platform organization row
Insert into `organizations` with:
- Fixed UUID (generate one and hardcode it)
- `name` = `OutVoice`
- `slug` = `outvoice`
- `country_id` = India's UUID from the V2 seed (look it up in V2__seed_iso_data.sql — iso_code_2 = 'IN')
- `currency_id` = INR's UUID from the V2 seed (look it up — code = 'INR')
- `created_by` = the owner user UUID we'll insert below
- `status` = `ACTIVE` (if the column doesn't exist yet, add it to organizations in this same migration with `ALTER TABLE organizations ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'`)

### 2. Platform owner user row
Insert into `users` with:
- Fixed UUID (generate one and hardcode it)
- `full_name` = `OutVoice Admin`
- `email` = read from env var `PLATFORM_OWNER_EMAIL` via a PL/pgSQL block (use `current_setting('app.platform_owner_email', true)` — fall back to `admin@outvoice.com` if null/empty)
- `password_hash` = read from env var `PLATFORM_OWNER_PASSWORD_HASH` via `current_setting('app.platform_owner_password_hash', true)` — this should be a pre-computed BCrypt hash. Fall back to a hardcoded BCrypt hash of `changeme123` if null/empty. Document clearly in a comment that this default must be changed before production.
- `country_id` = India's UUID (same as above)
- `is_email_verified` = `true`
- `created_at` / `updated_at` = `NOW()`

### 3. user_organizations join row
Insert into `user_organizations` with:
- Fixed UUID
- `user_id` = platform owner UUID
- `org_id` = platform org UUID
- `role` = `OWNER`
- `is_default_org` = `true`
- `status` = `ACTIVE`
- `joined_at` = `NOW()`

## How to pass env vars into Flyway/PostgreSQL

In `application.yaml` (and `application-local.yaml`), add under the Flyway config:

```yaml
spring:
  flyway:
    placeholders:
      platform_owner_email: ${PLATFORM_OWNER_EMAIL:admin@outvoice.com}
      platform_owner_password_hash: ${PLATFORM_OWNER_PASSWORD_HASH:<bcrypt-hash-of-changeme123>}
```

Then in the SQL migration use Flyway placeholders `${platform_owner_email}` and `${platform_owner_password_hash}` directly — this is simpler and more compatible than `current_setting()`. Use `INSERT ... ON CONFLICT DO NOTHING` on the fixed UUIDs so re-runs are safe.

## Checklist
- [ ] Fixed UUIDs are hardcoded (not generated at runtime)
- [ ] `ON CONFLICT DO NOTHING` on all three inserts
- [ ] `status` column added to `organizations` if not present
- [ ] Flyway placeholders wired in `application.yaml`
- [ ] A comment at the top of the SQL file listing the fixed UUIDs for reference
- [ ] Document the two env vars (`PLATFORM_OWNER_EMAIL`, `PLATFORM_OWNER_PASSWORD_HASH`) in the backend `CLAUDE.md` under Configuration → Required env vars