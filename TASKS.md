# OutVoice Backend — Tasks

## Configuration

### Configurable CORS [DONE]
- `app.cors-allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200}` — comma-separated; injected into `SecurityConfig.corsConfigurationSource()`

## Auth

### Sign up [DONE — UPDATED]
- `POST /api/auth/signup`
- Fields: full name (optional), email, mobile number, password
- **Breaking change from V1**: signup no longer creates an Organisation. V1 fields (organizationName, pan/gst/tan) removed.
- Creates only a `User` record; returns user-level JWT (no org context)
- Org creation is now Platform-Admin-only via `POST /api/platform/orgs`

### Login [DONE — UPDATED]
- `POST /api/auth/login`
- Body: `{ identifier, password }` (email or mobile)
- Returns:
  - Single org → org-scoped JWT with orgId/role/permissions claims
  - Multiple orgs → user-level JWT + `orgs[]` list for org picker
  - No org → user-level JWT + empty `orgs[]`

### Select Org [DONE]
- `POST /api/auth/select-org`
- Requires user-level JWT (after login with multiple orgs)
- Body: `{ orgId }`
- Returns org-scoped JWT

### Accept Invite [DONE]
- `POST /api/auth/invite/accept`
- Body: `{ token, fullName?, password? }`
- Creates user if not exists, links to org, returns scoped JWT

## Security

### JWT infrastructure [DONE — UPDATED]
- `JwtService` — now emits/parses org context claims (orgId, role, permissions[])
- `JwtAuthFilter` — extracts `UserContext` from JWT claims (no DB lookup per request)
- `SecurityConfig` — two filter chains: platform chain (`/api/platform/**`) + org chain
- `PlatformJwtService` / `PlatformJwtAuthFilter` — separate JWT for platform operators
- `@CurrentUser` annotation + `CurrentUserArgumentResolver` for clean controller injection
- `@RequiresRole` + `@RequiresPermission` annotations + `OrgAuthorizationAspect` (AOP)

## Persistence

### Database schema V1 [SUPERSEDED]
- V1 BIGSERIAL-based schema (organizations, users) — see V1__init_schema.sql

### Database schema V2 [DONE]
- Flyway migration `V2__org_role_model.sql`: drops V1 tables, recreates with UUID PKs
- Tables: organizations, users, org_users, org_invitations, audit_logs, platform_users
- Partial unique index: `one_owner_per_org` — enforces exactly 1 OWNER per org

## Org Management [DONE]

### APIs
- `GET /api/orgs/mine` — user's org list
- `GET /api/orgs/{orgId}` — org details
- `GET /api/orgs/{orgId}/members` — members list
- `POST /api/orgs/{orgId}/members/invite` — invite (MEMBER direct, SUPER_USER pending approval)
- `GET /api/orgs/{orgId}/invitations/pending` — Owner: pending SUPER_USER approvals
- `POST /api/orgs/{orgId}/invitations/{id}/approve` — Owner: approve SUPER_USER invite
- `POST /api/orgs/{orgId}/invitations/{id}/reject` — Owner: reject
- `PATCH /api/orgs/{orgId}/members/{userId}/permissions` — update MEMBER permissions
- `PATCH /api/orgs/{orgId}/members/{userId}/role` — change role (Owner only)
- `DELETE /api/orgs/{orgId}/members/{userId}` — suspend (soft-delete)
- `POST /api/orgs/{orgId}/transfer` — transfer ownership (password confirm required)
- `GET /api/orgs/{orgId}/audit-log` — paginated audit log (Owner only)

## Audit Log [DONE]
- `AuditLogService` — writes to `audit_logs` in a `REQUIRES_NEW` transaction
- Events: ORG_CREATED, MEMBER_INVITED, INVITE_APPROVED, INVITE_REJECTED, INVITE_ACCEPTED, ROLE_CHANGED, PERMISSIONS_CHANGED, MEMBER_SUSPENDED, OWNERSHIP_TRANSFERRED

## Platform Layer [DONE]
- Separate login: `POST /api/platform/auth/login`
- Org CRUD: `POST /api/platform/orgs`, `GET /api/platform/orgs`, `PATCH /{orgId}/suspend`, `PATCH /{orgId}/activate`
- `PlatformAdminSeeder` — seeds default Super Admin on first run from env vars
- Separate JWT secret: `PLATFORM_JWT_SECRET`

## Error handling [DONE — UPDATED]
- Added `BusinessException` (422), `NotFoundException` (404), `AccessDeniedException` (403)

## Rate limiting [DONE]
- Unchanged from V1

## Pending
- Invoice entity and PDF generation (OpenPDF dependency present, not wired up)
- Email notifications — invitation emails (Spring Mail dependency present, not wired up)
- Password reset flow
