package com.omkarsathe.outvoice.workspace.storefront;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class StorefrontUrlService {

    @Value("${frontend.url:https://dev.outvoice.omkarsathe.com}")
    private String frontendUrl;

    public String generatePublicUrl(Storefront storefront) {
        if (storefront == null) {
            return "";
        }
        UrlStrategy strategy = storefront.getUrlStrategy() != null ? storefront.getUrlStrategy() : UrlStrategy.PATH_BASED;
        String slug = storefront.getSlug();

        switch (strategy) {
            case OUTVOICE_SUBDOMAIN -> {
                return buildSubdomainUrl(slug);
            }
            case CUSTOM_DOMAIN -> {
                String primaryDomain = storefront.getDomains().stream()
                        .filter(StorefrontDomain::isPrimary)
                        .map(StorefrontDomain::getHostname)
                        .findFirst()
                        .orElse(null);

                if (primaryDomain != null && !primaryDomain.isBlank()) {
                    return "https://" + primaryDomain;
                }
                return buildPathBasedUrl(slug);
            }
            case PATH_BASED -> {
                return buildPathBasedUrl(slug);
            }
            default -> {
                return buildPathBasedUrl(slug);
            }
        }
    }

    public String buildPathBasedUrl(String slug) {
        String base = stripTrailingSlash(frontendUrl);
        return base + "/" + slug;
    }

    public String buildSubdomainUrl(String slug) {
        try {
            URI uri = URI.create(frontendUrl);
            String scheme = uri.getScheme() != null ? uri.getScheme() : "https";
            String host = uri.getHost() != null ? uri.getHost() : "dev.outvoice.omkarsathe.com";
            int port = uri.getPort();

            String fullHost = slug + "." + host;
            if (port != -1 && port != 80 && port != 443) {
                return scheme + "://" + fullHost + ":" + port;
            }
            return scheme + "://" + fullHost;
        } catch (Exception e) {
            return "https://" + slug + ".dev.outvoice.omkarsathe.com";
        }
    }

    public String extractSubdomainSlugFromHost(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }
        String cleanHost = ReservedSlugs.normalizeHostname(host);
        try {
            URI uri = URI.create(frontendUrl);
            String baseHost = uri.getHost() != null ? uri.getHost().toLowerCase() : "dev.outvoice.omkarsathe.com";
            if (cleanHost.endsWith("." + baseHost)) {
                String sub = cleanHost.substring(0, cleanHost.length() - baseHost.length() - 1);
                // In case of multi-level subdomain like a.b.dev.outvoice..., take the direct subdomain
                return sub.contains(".") ? sub.substring(sub.lastIndexOf('.') + 1) : sub;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String stripTrailingSlash(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
