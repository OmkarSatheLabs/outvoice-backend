package com.omkarsathe.outvoice.user;

import com.omkarsathe.outvoice.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("profile")
    public UserProfileResponse getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return userRepository.findById(userId)
                .map(userEntity -> new UserProfileResponse(
                        userEntity.getFullName(),
                        userEntity.getEmail(),
                        userEntity.getMobile(),
                        userEntity.getPhoneCode() != null ? userEntity.getPhoneCode().getId() : null,
                        null,
                        userEntity.getCountry() != null ? userEntity.getCountry().getId() : null
                ))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
