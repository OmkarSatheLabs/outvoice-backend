package com.omkarsathe.outvoice.auth;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.auth.dto.LoginRequest;
import com.omkarsathe.outvoice.auth.dto.SelectOrgRequest;
import com.omkarsathe.outvoice.auth.dto.SignupRequest;
import com.omkarsathe.outvoice.security.CurrentUser;
import com.omkarsathe.outvoice.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Exchanges a user-level JWT for a scoped org JWT.
     * Requires a valid (user-level) JWT in the Authorization header.
     */
    @PostMapping("/select-org")
    public AuthResponse selectOrg(@CurrentUser UserContext currentUser,
                                  @Valid @RequestBody SelectOrgRequest request) {
        return authService.selectOrg(currentUser, request);
    }

    /**
     * Accepts an org invitation via the secure token from the invite link.
     * Links the user to the org (creates user if they don't exist yet).
     */
    @PostMapping("/invite/accept")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        return authService.acceptInvite(request);
    }
}
