package com.omkarsathe.outvoice.workspace.user;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServicePasswordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .fullName("Alice Developer")
                .email("alice@outvoice.app")
                .passwordHash("currentHashedPass")
                .build();

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(userId.toString());
    }

    @Test
    @DisplayName("Should successfully change password when current password matches")
    void shouldChangePasswordSuccessfully() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPass123", "currentHashedPass")).thenReturn(true);
        when(passwordEncoder.matches("NewPass123", "currentHashedPass")).thenReturn(false);
        when(passwordEncoder.encode("NewPass123")).thenReturn("newHashedPass");

        userService.changePassword(userDetails, new ChangePasswordRequest("OldPass123", "NewPass123"));

        assertEquals("newHashedPass", testUser.getPasswordHash());
        verify(userRepository).save(testUser);
        verify(auditService).log(isNull(), eq(AuditAction.UPDATE), eq(AuditResourceType.USER), eq(testUser.getId()), any(), isNull());
    }

    @Test
    @DisplayName("Should reject password change when current password is wrong")
    void shouldRejectWhenCurrentPasswordIsWrong() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPass", "currentHashedPass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () ->
                userService.changePassword(userDetails, new ChangePasswordRequest("WrongPass", "NewPass123")));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject password change when new password is same as current")
    void shouldRejectWhenNewPasswordSameAsCurrent() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPass123", "currentHashedPass")).thenReturn(true);
        when(passwordEncoder.matches("OldPass123", "currentHashedPass")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.changePassword(userDetails, new ChangePasswordRequest("OldPass123", "OldPass123")));

        verify(userRepository, never()).save(any());
    }
}
