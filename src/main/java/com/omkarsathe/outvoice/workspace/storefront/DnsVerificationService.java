package com.omkarsathe.outvoice.workspace.storefront;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Hashtable;

@Service
@Slf4j
public class DnsVerificationService {

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.storefront.bypass-dns-verification:false}")
    private boolean bypassDnsVerification;

    public String generateVerificationToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return "ov_verify_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public boolean verifyChallenge(String hostname, String expectedToken) {
        if (bypassDnsVerification) {
            log.info("Bypassing DNS verification for {} due to configuration", hostname);
            return true;
        }

        String recordHost = "_outvoice-challenge." + hostname;
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            env.put(Context.PROVIDER_URL, "dns:");

            DirContext dirContext = new InitialDirContext(env);
            Attributes attrs = dirContext.getAttributes(recordHost, new String[]{"TXT"});
            Attribute txtAttr = attrs.get("TXT");

            if (txtAttr != null) {
                NamingEnumeration<?> e = txtAttr.getAll();
                while (e.hasMore()) {
                    String value = (String) e.next();
                    // Clean possible DNS quotes
                    String cleanValue = value.replace("\"", "").trim();
                    if (cleanValue.equals(expectedToken.trim())) {
                        log.info("DNS verification successful for {} at {}", hostname, recordHost);
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("DNS query failed for {}: {}", recordHost, ex.getMessage());
        }
        return false;
    }
}
