# OutVoice Backend — Tasks

## Auth

### Sign up [DONE]
- `POST /api/auth/signup`
- Fields: full name, email (optional if mobile provided), mobile number (optional if email provided), password, organization name, tax compliance name (defaults to full name), PAN, GST, TAN (all optional)
- Custom validator `@EmailOrMobileRequired` enforces email-or-mobile constraint
- Creates `Organization` and `User` records in a single transaction; returns JWT
- Duplicate guard: if `app.allow-duplicate-signup=true`, returns existing user's token instead of 409

### Login [DONE]
- `POST /api/auth/login`
- Fields: `identifier` (email or mobile number), `password`
- Looks up user by email or mobile; validates BCrypt password hash; returns JWT on success
- Returns 401 on bad credentials via `BadCredentialsException → GlobalExceptionHandler`

## Security

### JWT infrastructure [DONE]
- `JwtService` — HS256 token generation and validation via JJWT 0.12.6
- `JwtAuthFilter` — `OncePerRequestFilter`; extracts Bearer token from `Authorization` header and sets `SecurityContext`
- `SecurityConfig` — stateless session policy; CORS configured for `http://localhost:4200`; `/api/auth/**` is public; all other routes require authentication

## Persistence

### Database schema V1 [DONE]
- Flyway migration `V1__init_schema.sql`: `organizations` and `users` tables
- `organizations`: id, name, tax_compliance_name, pan_number, gst_number, tan_number, created_at
- `users`: id, full_name, email (unique), mobile_number (unique), password_hash, organization_id (FK), created_at

## Error handling

### Global exception handler [DONE]
- `GlobalExceptionHandler` handles `MethodArgumentNotValidException` (400), `DataIntegrityViolationException` (409 duplicate), `BadCredentialsException` (401)
- All errors return `ApiError` shape: `{ status, message, errors[] }`

## Pending

- Invoice entity and PDF generation (OpenPDF dependency present, not wired up)
- Email notifications (Spring Mail dependency present, not wired up)
- Protected endpoints (e.g., invoice CRUD) requiring JWT auth
