package com.omkarsathe.outvoice.workspace.user;

import com.omkarsathe.outvoice.workspace.WorkspaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
}
