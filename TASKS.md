# OutVoice Backend — Tasks

## Configuration

### Configurable CORS [DONE]
- `app.cors-allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200}` — comma-separated; injected into `SecurityConfig.corsConfigurationSource()`

## Reference data

### Country / Currency / Phone-code APIs [DONE]
- `GET /api/reference/countries` — list of active countries with default currency
- `GET /api/reference/currencies` — list of active currencies
- `GET /api/reference/phone-codes` — list of all phone codes
- `GET /api/reference/countries/{isoCode2}/primary-phone-code` — primary dial code for a country
- Packages: `country/`, `currency/`, `phone/`, `entity/` (CountryPhoneCode join)
- All reference endpoints are public (`/api/reference/**` added to SecurityConfig permitAll)
- Data seeded via Flyway `V2__seed_iso_data.sql` (~155 currencies, ~250 countries, phone codes)

## Auth

### Sign up [DONE]
- `POST /api/auth/signup`
- Required fields: `fullName`, password, `userCountryId`, `organizationName`, `organizationSlug`, `currencyId`
- Optional fields: `email` (or `mobile` + `phoneCodeId` — at least one required), `organizationCountryId` (defaults to `userCountryId`), `taxComplianceName`
- Custom validator `@EmailOrMobileRequired` enforces email-or-mobile constraint
- Flow: resolves Country/Currency/PhoneCode by UUID → saves User → saves Organization → saves UserOrganization (role=OWNER, isDefaultOrg=true, status=ACTIVE)
- Duplicate guard: throws 401 `BadCredentialsException` if email or mobile already exists

### Login [DONE]
- `POST /api/auth/login`
- Fields: `identifier` (email or mobile number), `password`
- Looks up user by email or mobile; validates BCrypt password hash; returns JWT on success
- Returns 401 with server message on bad credentials via `BadCredentialsException → GlobalExceptionHandler`

## Security

### JWT infrastructure [DONE]
- `JwtService` — HS256 token generation and validation via JJWT 0.12.6
- `JwtAuthFilter` — `OncePerRequestFilter`; extracts Bearer token from `Authorization` header and sets `SecurityContext`
- `SecurityConfig` — stateless session policy; CORS configured via env var; `/api/auth/**` and `/api/reference/**` are public; all other routes require authentication

## Persistence

### Database schema V1 [DONE]
- Flyway migration `V1__init_schema.sql` — all PKs are UUID
- `currencies`: id (UUID), code (ISO 4217), name, symbol, decimal_places
- `countries`: id (UUID), name, iso_code_2, iso_code_3, default_currency_id (FK)
- `phone_codes`: id (UUID), code (e.g. +91)
- `country_phone_codes`: join table with `is_primary` flag; unique index enforces one primary per country
- `users`: id (UUID), email (unique), phone_code_id (FK), mobile (unique), full_name, password_hash, is_email_verified, is_mobile_verified, country_id (FK), created_at, updated_at, deleted_at
- `organizations`: id (UUID), name, slug (unique), country_id (FK), currency_id (FK), tax_compliance_name, pan_number, gst_number, tan_number, created_by (FK → users), created_at, updated_at, deleted_at
- `user_organizations`: id (UUID), user_id (FK), org_id (FK), role, is_default_org, invited_by (FK), joined_at, status

### ISO seed data V2 [DONE]
- Flyway migration `V2__seed_iso_data.sql` — seeds currencies, countries, phone codes and country_phone_codes with is_primary flag

## Error handling

### Global exception handler [DONE]
- `GlobalExceptionHandler` handles:
  - `MethodArgumentNotValidException` → 400
  - `BadCredentialsException` → 401 (propagates server message to client)
  - `DataIntegrityViolationException` → 409 duplicate
  - `ResponseStatusException` → passes through status + reason
  - `Exception` → 500 generic fallback
- All errors return `ApiError` shape: `{ status, message, errors[] }`

## Rate limiting

### Auth endpoint rate limiter [DONE]
- `RateLimitFilter` — `OncePerRequestFilter` + `@Component`; applies only to `/api/auth/signup` and `/api/auth/login` via `shouldNotFilter()`
- Per-IP token bucket via Bucket4j 8.18.0 (`bucket4j_jdk17-core`)
- IP resolved from `X-Forwarded-For` header (first value) or `RemoteAddr` fallback
- Default: 10 requests per 60-second window per IP; configurable via `RATE_LIMIT_AUTH_CAPACITY` / `RATE_LIMIT_AUTH_REFILL_SECONDS` env vars
- Returns HTTP 429 with `{ status, message, errors[] }` body matching `ApiError` shape
- Note: buckets are in-memory (`ConcurrentHashMap`) — not shared across multiple instances; revisit if running replicated

## Pending

- Invoice entity and PDF generation (OpenPDF dependency present, not wired up)
- Email notifications (Spring Mail dependency present, not wired up)
- Protected endpoints (e.g., invoice CRUD) requiring JWT auth
- Email / mobile verification flow (`is_email_verified`, `is_mobile_verified` columns are in place)
