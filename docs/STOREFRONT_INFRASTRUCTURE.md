# OutVoice Multi-Tenant Storefront Infrastructure & Operational Guide

This document describes how to configure reverse proxies (Nginx, Caddy, Cloudflare) and DNS to route all three storefront URL strategies to OutVoice:

1. **Option A (Path-based):** `https://dev.outvoice.omkarsathe.com/{workspace-slug}`
2. **Option B (Subdomain):** `https://{workspace-slug}.dev.outvoice.omkarsathe.com`
3. **Option C (Custom Domain):** `https://store.customerdomain.com`

---

## 1. Architecture Overview

```
                      +---------------------------------------+
                      |               Internet                |
                      +-------------------+-------------------+
                                          |
        +---------------------------------+---------------------------------+
        |                                 |                                 |
 [Path: /acme-shop]             [Subdomain: acme.dev.*]             [Custom Domain: store.acme.com]
        |                                 |                                 |
        +-------------------------------->+<--------------------------------+
                                          |
                                          v
                      +---------------------------------------+
                      |         Edge Proxy / Ingress          |
                      |   (Nginx / Caddy / Cloudflare SSL)    |
                      +-------------------+-------------------+
                                          |
                +-------------------------+-------------------------+
                |                                                   |
                v                                                   v
   +--------------------------+                        +--------------------------+
   |   outvoice-web (SPA)     |                        |  outvoice-backend (API)  |
   | Angular HTML5 fallback   |                        | /api/public/storefronts/ |
   | HostRoutingService       |                        | ForwardedHeaderFilter    |
   +--------------------------+                        +--------------------------+
```

---

## 2. Ingress & Reverse Proxy Configurations

### 2.1 Caddy (Recommended for automatic TLS on custom domains)

Caddy's `on_demand_tls` feature allows automatic provisioning and renewal of Let's Encrypt / ZeroSSL certificates for custom customer domains dynamically:

```caddy
{
    # Only issue certificates for hostnames verified in OutVoice backend
    on_demand_tls {
        ask http://localhost:8080/api/public/storefronts/resolve/by-host
        interval 2m
        burst 5
    }
}

# Main Application & Subdomains (*.dev.outvoice.omkarsathe.com)
*.dev.outvoice.omkarsathe.com, dev.outvoice.omkarsathe.com {
    # Forward API calls to Spring Boot
    handle /api/* {
        reverse_proxy localhost:8080 {
            header_up Host {host}
            header_up X-Forwarded-Host {host}
            header_up X-Forwarded-Proto {scheme}
        }
    }

    # Serve Angular SPA with HTML5 pushState fallback
    handle {
        root * /var/www/outvoice-web/dist/outvoice-web/browser
        try_files {path} /index.html
        file_server
    }
}

# Customer-owned Custom Domains (Option C)
:443 {
    tls {
        on_demand
    }

    # API endpoints
    handle /api/* {
        reverse_proxy localhost:8080 {
            header_up Host {host}
            header_up X-Forwarded-Host {host}
            header_up X-Forwarded-Proto {scheme}
        }
    }

    # Angular SPA
    handle {
        root * /var/www/outvoice-web/dist/outvoice-web/browser
        try_files {path} /index.html
        file_server
    }
}
```

---

### 2.2 Nginx Configuration

```nginx
# Map for Host forwarding
server {
    listen 80;
    listen [::]:80;
    server_name dev.outvoice.omkarsathe.com *.dev.outvoice.omkarsathe.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name dev.outvoice.omkarsathe.com *.dev.outvoice.omkarsathe.com;

    ssl_certificate /etc/letsencrypt/live/outvoice/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/outvoice/privkey.pem;

    root /var/www/outvoice-web/dist/outvoice-web/browser;
    index index.html;

    # Spring Boot REST API
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Port $server_port;
    }

    # Angular SPA (HTML5 routing fallback)
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

---

## 3. Custom Domain Verification (DNS TXT Records)

When a customer connects a domain `store.acme.com`:
1. The backend generates a verification challenge:
   - **Type:** `TXT`
   - **Host:** `_outvoice-challenge.store.acme.com`
   - **Value:** `outvoice-verify={32_HEX_TOKEN}`
2. The customer adds this TXT record and a `CNAME` pointing to `dev.outvoice.omkarsathe.com`.
3. In Storefront Management, clicking **Verify DNS** prompts `DnsVerificationService` to execute a DNS TXT lookup.
4. When verified, `verification_status` transitions to `VERIFIED` and `tls_status` transitions to `ACTIVE`.

---

## 4. Backend Configuration Reference

In `application.yaml` / environment variables:

| Variable | Default Value | Description |
|---|---|---|
| `APP_STOREFRONT_BASE_DOMAIN` | `dev.outvoice.omkarsathe.com` | Base domain for tenant subdomains |
| `APP_STOREFRONT_DEFAULT_FRONTEND_URL` | `http://localhost:4200` | Canonical origin for path-based storefronts |
| `APP_STOREFRONT_AUTO_VERIFY_DNS` | `false` | When `true`, auto-verifies DNS challenges in local/test environments |
