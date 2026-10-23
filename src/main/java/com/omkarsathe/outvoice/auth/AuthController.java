package com.omkarsathe.outvoice.auth;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.auth.mfa.dto.MfaResendRequest;
import com.omkarsathe.outvoice.auth.mfa.dto.MfaSetupResponse;
import com.omkarsathe.outvoice.auth.mfa.dto.MfaVerifyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/mfa/verify")
    public AuthResponse verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        return authService.verifyMfa(request);
    }

    @PostMapping("/mfa/resend")
    public Map<String, String> resendMfa(@Valid @RequestBody MfaResendRequest request) {
        return authService.resendMfa(request);
    }

    @PostMapping("/mfa/fallback-email")
    public MfaSetupResponse fallbackEmailMfa(@Valid @RequestBody MfaResendRequest request) {
        return authService.fallbackEmailMfa(request);
    }
}
