CREATE TABLE currencies (
    id             UUID         PRIMARY KEY,
    code           VARCHAR(3)   NOT NULL UNIQUE,   -- ISO 4217 e.g. INR, USD, EUR
    name           VARCHAR(100) NOT NULL,           -- e.g. Indian Rupee
    symbol         VARCHAR(10)  NOT NULL,           -- e.g. ₹, $, €
    decimal_places SMALLINT     NOT NULL DEFAULT 2, -- 0 for JPY, 3 for KWD
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE countries (
    id                  UUID         PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    iso_code_2          VARCHAR(2)   NOT NULL UNIQUE,  -- e.g. IN, US, GB
    iso_code_3          VARCHAR(3)   NOT NULL UNIQUE,  -- e.g. IND, USA, GBR
    default_currency_id UUID         REFERENCES currencies,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE phone_codes (
    id         UUID        PRIMARY KEY,
    code       VARCHAR(10) NOT NULL UNIQUE,  -- e.g. +91, +1, +44
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE country_phone_codes (
    id            UUID      PRIMARY KEY,
    country_id    UUID      NOT NULL REFERENCES countries,
    phone_code_id UUID      NOT NULL REFERENCES phone_codes,
    is_primary    BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_country_phone_code UNIQUE (country_id, phone_code_id)
);

CREATE UNIQUE INDEX uq_country_primary_phone_code
    ON country_phone_codes (country_id)
    WHERE is_primary = TRUE;

CREATE TABLE users (
    id                 UUID PRIMARY KEY,
    email              VARCHAR(255) UNIQUE,
    phone_code_id      UUID REFERENCES phone_codes,
    mobile             VARCHAR(20)  UNIQUE,
    full_name          VARCHAR(255) NOT NULL,
    password_hash      VARCHAR(255) NOT NULL,
    is_email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    is_mobile_verified BOOLEAN NOT NULL DEFAULT FALSE,
    country_id         UUID REFERENCES countries,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at         TIMESTAMP DEFAULT null
);

CREATE TABLE organizations (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    country_id          UUID REFERENCES countries,
    currency_id         UUID REFERENCES currencies,
    tax_compliance_name VARCHAR(255) NOT NULL,
    pan_number          VARCHAR(20),
    gst_number          VARCHAR(20),
    tan_number          VARCHAR(20),
    created_by          UUID REFERENCES users,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP DEFAULT null
);

CREATE TABLE user_organizations (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users,
    org_id UUID REFERENCES organizations,
    role VARCHAR NOT NULL,
    is_default_org BOOLEAN DEFAULT false,
    invited_by UUID REFERENCES users,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR
);
