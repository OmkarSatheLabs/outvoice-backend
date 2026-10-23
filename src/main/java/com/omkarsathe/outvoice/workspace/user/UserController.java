package com.omkarsathe.outvoice.workspace.user;

import com.omkarsathe.outvoice.auth.mfa.dto.MfaConfirmRequest;
import com.omkarsathe.outvoice.auth.mfa.dto.MfaSetupResponse;
import com.omkarsathe.outvoice.auth.mfa.dto.TotpConfirmRequest;
import com.omkarsathe.outvoice.auth.mfa.dto.TotpSetupResponse;
import com.omkarsathe.outvoice.workspace.WorkspaceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{userId}/workspaces")
    public List<WorkspaceResponse> getWorkspaces(@PathVariable UUID userId) {
        return userService.getWorkspaces(userId);
    }

    @GetMapping("profile")
    public UserProfileResponse getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserProfile(userDetails);
    }

    @PostMapping("change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails, request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    @PostMapping("mfa/totp/setup")
    public TotpSetupResponse initiateTotpSetup(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.initiateTotpSetup(userDetails);
    }

    @PostMapping("mfa/totp/confirm")
    public ResponseEntity<Map<String, Object>> confirmTotpSetup(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TotpConfirmRequest request
    ) {
        userService.confirmTotpSetup(userDetails, request);
        return ResponseEntity.ok(Map.of("mfaEnabled", true, "mfaChannel", MfaChannel.AUTHENTICATOR_APP.name()));
    }

    @PostMapping("mfa/email/setup")
    public MfaSetupResponse initiateEmailSetup(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.initiateEmailSetup(userDetails);
    }

    @PostMapping("mfa/email/confirm")
    public ResponseEntity<Map<String, Object>> confirmEmailSetup(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MfaConfirmRequest request
    ) {
        userService.confirmEmailSetup(userDetails, request);
        return ResponseEntity.ok(Map.of("mfaEnabled", true));
    }

    @PostMapping("mfa/disable")
    public ResponseEntity<Map<String, Object>> disableMfa(@AuthenticationPrincipal UserDetails userDetails) {
        userService.disableMfa(userDetails);
        return ResponseEntity.ok(Map.of("mfaEnabled", false));
    }
}
