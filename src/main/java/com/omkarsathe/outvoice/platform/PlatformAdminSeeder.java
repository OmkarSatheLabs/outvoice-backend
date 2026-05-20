package com.omkarsathe.outvoice.platform;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Seeds a default Platform Super Admin on first run.
 * Credentials are read from env vars PLATFORM_ADMIN_EMAIL and PLATFORM_ADMIN_PASSWORD.
 * Logs a warning if the default seeded credentials match common values (dev guard).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformAdminSeeder implements ApplicationRunner {

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${platform.admin.email:admin@outvoice.internal}")
    private String adminEmail;

    @Value("${platform.admin.password:changeme-immediately}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (platformUserRepository.existsByEmail(adminEmail)) {
            return; // already seeded
        }

        PlatformUser superAdmin = PlatformUser.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(PlatformRole.SUPER_ADMIN)
                .status(PlatformStatus.ACTIVE)
                .build();
        platformUserRepository.save(superAdmin);

        log.info("Platform Super Admin seeded with email: {}", adminEmail);

        if ("changeme-immediately".equals(adminPassword)) {
            log.warn("WARNING: Platform Super Admin is using the default password. " +
                     "Set PLATFORM_ADMIN_PASSWORD env var and restart to change it.");
        }
    }
}
