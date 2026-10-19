package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.common.exception.BadRequestException;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class ReservedSlugs {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private static final Set<String> RESERVED_SLUGS = Set.of(
            "admin",
            "api",
            "app",
            "auth",
            "assets",
            "actuator",
            "billing",
            "checkout",
            "customers",
            "dashboard",
            "dev",
            "docs",
            "forgot-password",
            "health",
            "help",
            "invoices",
            "join",
            "legal",
            "login",
            "mail",
            "products",
            "public",
            "reset-password",
            "root",
            "settings",
            "signup",
            "staging",
            "status",
            "storefront",
            "support",
            "test",
            "view",
            "webhooks",
            "workspaces",
            "www"
    );

    private ReservedSlugs() {
    }

    public static String normalizeSlug(String slug) {
        if (slug == null) {
            return "";
        }
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeHostname(String hostname) {
        if (hostname == null) {
            return "";
        }
        String clean = hostname.trim().toLowerCase(Locale.ROOT);
        // Strip any protocol if mistakenly supplied
        clean = clean.replaceFirst("^https?://", "");
        // Strip any port or trailing path
        int slashIdx = clean.indexOf('/');
        if (slashIdx != -1) {
            clean = clean.substring(0, slashIdx);
        }
        int colonIdx = clean.indexOf(':');
        if (colonIdx != -1) {
            clean = clean.substring(0, colonIdx);
        }
        return clean;
    }

    public static boolean isReserved(String slug) {
        if (slug == null) {
            return false;
        }
        return RESERVED_SLUGS.contains(normalizeSlug(slug));
    }

    public static void validateSlug(String slug) {
        String normalized = normalizeSlug(slug);
        if (normalized.length() < 3 || normalized.length() > 63) {
            throw new BadRequestException("Slug length must be between 3 and 63 characters");
        }
        if (!SLUG_PATTERN.matcher(normalized).matches()) {
            throw new BadRequestException("Slug must contain only lowercase alphanumeric characters and hyphens, and cannot start or end with a hyphen");
        }
        if (isReserved(normalized)) {
            throw new BadRequestException("Slug '" + normalized + "' is reserved and cannot be used");
        }
    }
}
