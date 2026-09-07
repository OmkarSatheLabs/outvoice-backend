package com.omkarsathe.outvoice.workspace.user;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.common.exception.UserAlreadyExistsException;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.phone.PhoneCodeMapper;
import com.omkarsathe.outvoice.phone.PhoneCodeService;
import com.omkarsathe.outvoice.user.workspace.role.RoleNotFoundException;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.WorkspaceResponseMapper;
import com.omkarsathe.outvoice.workspace.WorkspaceResponse;
import com.omkarsathe.outvoice.workspace.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PhoneCodeService phoneCodeService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PhoneCodeMapper phoneCodeMapper;
    private final MemberRepository memberRepository;
    private final WorkspaceResponseMapper workspaceMapper;
    private final UserProfileResponseMapper userProfileResponseMapper;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceResponseMapper workspaceResponseMapper;

    public UserResponse create(CreateUser request) {
        PhoneCode phoneCode = null;
        String email = null;

        if (request.phoneCode() != null && !request.phoneCode().isBlank()) {
            phoneCode = phoneCodeService.findByCode(request.phoneCode());
        }

        if (request.email() != null && !request.email().isBlank()) {
            email = request.email();
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("User with email " + email + " already exists");
            }
        }

        User user = User.builder()
                .email(request.email())
                .phoneCode(phoneCode)
                .mobile(request.mobile())
                .fullName(request.fullName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        User newUser = userRepository.save(user);

        return new UserResponse(
                newUser.getId(),
                newUser.getFullName(),
                newUser.getEmail(),
                newUser.getPhoneCode() != null
                        ? phoneCodeMapper.toResponse(newUser.getPhoneCode())
                        : null,
                newUser.getMobile()
        );
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("User not found: " + id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<WorkspaceResponse> getWorkspaces(UUID userId) {
        return memberRepository.findByUserId(userId)
                .stream()
                .map(member -> workspaceResponseMapper.toResponse(member.getWorkspace()))
                .toList();
    }

    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserProfileResponse getUserProfile(UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return userRepository.findById(userId)
                .map(userProfileResponseMapper::toUserProfileResponse)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
