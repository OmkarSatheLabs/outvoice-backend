package com.omkarsathe.outvoice.platform;

import com.omkarsathe.outvoice.platform.dto.PlatformLoginRequest;
import com.omkarsathe.outvoice.platform.dto.PlatformLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Handles authentication for Outvoice platform (internal) operators. */
@Service
@RequiredArgsConstructor
public class PlatformAuthService {

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformJwtService platformJwtService;

    public PlatformLoginResponse login(PlatformLoginRequest request) {
        PlatformUser user = platformUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid platform credentials"));

        if (user.getStatus() == PlatformStatus.SUSPENDED) {
            throw new BadCredentialsException("Platform account is suspended");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid platform credentials");
        }

        String token = platformJwtService.generateToken(user.getId(), user.getRole());
        return new PlatformLoginResponse(token, user.getId(), user.getRole());
    }
}
