# Org/Role Model — Backend

## Overview

Two completely separate authentication layers:

| Layer | Users | Login endpoint | JWT secret |
|---|---|---|---|
| **Org layer** | Customer users | `POST /api/auth/login` | `JWT_SECRET` |
| **Platform layer** | Outvoice operators | `POST /api/platform/auth/login` | `PLATFORM_JWT_SECRET` |

---

## Entity Relationships

```
organizations (id UUID, name, slug, owner_user_id FK→users, status, created_at, updated_at)
    │
    └──< org_users (id UUID, org_id FK, user_id FK, role, permissions TEXT, invited_by FK, status)
                        │
                        └── UNIQUE(org_id, user_id)
                        └── UNIQUE INDEX one_owner_per_org ON org_users(org_id)
                              WHERE role = 'OWNER' AND status = 'ACTIVE'

users (id UUID, full_name, email UNIQUE, mobile_number UNIQUE, password_hash, email_verified, mobile_verified, status)
    CHECK (email IS NOT NULL OR mobile_number IS NOT NULL)

org_invitations (id UUID, org_id FK, invited_by FK, invitee_email, invitee_mobile,
                 intended_role, intended_permissions TEXT, approval_status, approved_by FK,
                 token UNIQUE, expires_at, status)

audit_logs (id UUID, org_id FK nullable, actor_user_id FK, action, target_user_id FK nullable,
            old_value TEXT, new_value TEXT, ip_address, created_at)

platform_users (id UUID, email UNIQUE, password_hash, role, status, created_at)
```

---

## Role Hierarchy

```
Organization
    └── OWNER        (exactly 1 per org; enforced by partial unique index)
    └── SUPER_USER   (0..N; full permissions, no transfer privilege)
    └── MEMBER       (0..N; only explicitly granted permissions)
```

Owner is a flag on a Super User (same `org_users` record). It is NOT a separate entity.

---

## Permissions (MEMBER only)

| Permission | Description |
|---|---|
| `INVOICE_READ` | View invoices |
| `INVOICE_WRITE` | Create/edit invoices |
| `INVOICE_DELETE` | Delete invoices |
| `INVOICE_EXPORT` | Export invoices to PDF/CSV |
| `CLIENT_READ` | View clients |
| `CLIENT_WRITE` | Create/edit clients |
| `CLIENT_DELETE` | Delete clients |
| `REPORT_READ` | View reports |
| `REPORT_EXPORT` | Export reports |
| `MANAGE_MEMBERS` | Invite members (own org only) |

OWNER and SUPER_USER receive all permissions automatically. The `permissions` column on `org_users` is `NULL` for these roles.

---

## JWT Payload Structures

### Org-layer JWT

```json
{
  "sub": "<userId UUID>",
  "orgId": "<orgId UUID>",         // absent for user-level token
  "role": "OWNER|SUPER_USER|MEMBER", // absent for user-level token
  "permissions": ["INVOICE_READ"], // only present for MEMBER
  "iat": 1234567890,
  "exp": 1234654290
}
```

### Platform JWT

```json
{
  "sub": "<platformUserId UUID>",
  "platformRole": "SUPER_ADMIN|ADMIN|FINANCE|SUPPORT",
  "iat": 1234567890,
  "exp": 1234654290
}
```

---

## API Endpoints

### Org-layer Auth (`/api/auth/**` — public)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/signup` | — | Create user (no org) |
| POST | `/api/auth/login` | — | Login; returns user-level or org-scoped JWT |
| POST | `/api/auth/select-org` | user JWT | Exchange user token for org-scoped token |
| POST | `/api/auth/invite/accept` | — | Accept invitation by token |

**Login response:**
- `orgs` is `null` → single org, fully scoped JWT issued.
- `orgs` is `[]` → no org, user-level JWT; show empty state.
- `orgs` is non-empty list → multi-org, user-level JWT; show org picker.

### Org Management (`/api/orgs/**` — org JWT required)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/orgs/mine` | org JWT | All orgs the caller belongs to |
| GET | `/api/orgs/{orgId}` | member | Org details |
| GET | `/api/orgs/{orgId}/members` | member | All members with roles/permissions |
| POST | `/api/orgs/{orgId}/members/invite` | SUPER_USER or OWNER | Invite a user |
| GET | `/api/orgs/{orgId}/invitations/pending` | OWNER | Pending Super User approvals |
| POST | `/api/orgs/{orgId}/invitations/{id}/approve` | OWNER | Approve a Super User invite |
| POST | `/api/orgs/{orgId}/invitations/{id}/reject` | OWNER | Reject a Super User invite |
| PATCH | `/api/orgs/{orgId}/members/{userId}/permissions` | OWNER or SUPER_USER | Update MEMBER permissions |
| PATCH | `/api/orgs/{orgId}/members/{userId}/role` | OWNER | Promote/demote a member |
| DELETE | `/api/orgs/{orgId}/members/{userId}` | OWNER or SUPER_USER | Suspend (soft-delete) a member |
| POST | `/api/orgs/{orgId}/transfer` | OWNER | Transfer ownership (password re-confirm) |

### Platform Auth (`/api/platform/auth/**` — public)

| Method | Path | Description |
|---|---|---|
| POST | `/api/platform/auth/login` | Authenticate a platform operator |

### Platform Org Management (`/api/platform/orgs/**` — platform JWT required)

| Method | Path | Description |
|---|---|---|
| POST | `/api/platform/orgs` | Create org, assign owner |
| GET | `/api/platform/orgs` | List all orgs with stats |
| PATCH | `/api/platform/orgs/{orgId}/suspend` | Suspend an org |
| PATCH | `/api/platform/orgs/{orgId}/activate` | Activate an org |

---

## Business Rules Enforced

1. **Exactly 1 OWNER per org** — `CREATE UNIQUE INDEX one_owner_per_org ON org_users(org_id) WHERE role = 'OWNER' AND status = 'ACTIVE'`
2. **Owner is the only role that can transfer ownership** — enforced by `@RequiresRole(OrgRole.OWNER)` on `/transfer`
3. **Transfer only to existing Super User** — validated in `OrgService.transferOwnership`
4. **Owner cannot be suspended** — enforced in `OrgService.suspendMember`
5. **Super Users can manage Members only** — enforced in `OrgService.suspendMember` and `updatePermissions`
6. **Super User invites require Owner approval** — `intendedRole = SUPER_USER` → `approvalStatus = PENDING`
7. **Member invites by Super Users go directly** — `intendedRole = MEMBER` → `approvalStatus = APPROVED`
8. **Multi-org users select an org post-login** — login returns `orgs[]`; `/auth/select-org` issues scoped JWT
9. **Removed users are soft-deleted** — `status = SUSPENDED`, hard deletes never happen

---

## Audit Log Events

| Event | When |
|---|---|
| `ORG_CREATED` | Platform Admin creates an org |
| `MEMBER_INVITED` | Any eligible user sends an invitation |
| `INVITE_APPROVED` | Owner approves a Super User invite |
| `INVITE_REJECTED` | Owner rejects a Super User invite |
| `INVITE_ACCEPTED` | Invitee accepts via token |
| `ROLE_CHANGED` | Owner promotes/demotes a member |
| `PERMISSIONS_CHANGED` | Owner or Super User updates a Member's permissions |
| `MEMBER_SUSPENDED` | Owner or Super User suspends a member |
| `OWNERSHIP_TRANSFERRED` | Owner transfers to a Super User |

---

## Authorization Design

### Dual guards (defense-in-depth)

1. **Controller-level**: `@RequiresRole` and `@RequiresPermission` annotations processed by `OrgAuthorizationAspect` (Spring AOP).
2. **Service-level**: explicit `assertRole(ctx, ...)` and business-rule checks inside service methods.

### Two security filter chains (Spring Security)

- **Platform chain** (`@Order(1)`, matcher `/api/platform/**`): validates platform JWTs via `PlatformJwtAuthFilter`. Sets `PlatformContext` as principal.
- **Org chain** (`@Order(2)`, matcher `/**`): validates org JWTs via `JwtAuthFilter`. Sets `UserContext` as principal. No DB lookup per request — all context extracted from JWT claims.

### `@CurrentUser` injection

```java
@GetMapping("/me")
public ResponseEntity<?> me(@CurrentUser UserContext ctx) {
    // ctx.userId(), ctx.orgId(), ctx.role(), ctx.permissions()
}
```

Resolved by `CurrentUserArgumentResolver` from the `Authentication.getPrincipal()` in `SecurityContext`.

---

## Notable Design Decisions

- **No DB lookup per authenticated request**: `JwtAuthFilter` extracts all user/org context from JWT claims. This avoids N DB queries for N concurrent requests. The trade-off is that role/permission changes only take effect on the next login/select-org cycle.
- **Separate platform JWT secret**: platform tokens and org tokens cannot be swapped even if one secret leaks.
- **Soft deletes throughout**: no hard deletes. `OrgUserStatus.SUSPENDED` is the terminal state for removed members. Their data references remain intact.
- **Permissions as TEXT (JSON)**: stored as a JSON array in a TEXT column, serialized by `PermissionListConverter`. Simpler than a many-to-many table for a small, fixed permission set.
- **Flyway V2 drops V1 schema**: V1 used BIGSERIAL PKs and a monolithic org+user model. V2 is a clean UUID-based redesign. Acceptable at this early-dev stage.
