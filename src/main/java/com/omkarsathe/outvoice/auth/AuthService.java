package com.omkarsathe.outvoice.auth;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.auth.dto.LoginRequest;
import com.omkarsathe.outvoice.auth.dto.OrgSummaryDto;
import com.omkarsathe.outvoice.auth.dto.SelectOrgRequest;
import com.omkarsathe.outvoice.auth.dto.SignupRequest;
import com.omkarsathe.outvoice.common.exception.BusinessException;
import com.omkarsathe.outvoice.organization.InvitationStatus;
import com.omkarsathe.outvoice.organization.OrgInvitation;
import com.omkarsathe.outvoice.organization.OrgInvitationRepository;
import com.omkarsathe.outvoice.organization.OrgRole;
import com.omkarsathe.outvoice.organization.OrgUser;
import com.omkarsathe.outvoice.organization.OrgUserRepository;
import com.omkarsathe.outvoice.organization.OrgUserStatus;
import com.omkarsathe.outvoice.organization.Organization;
import com.omkarsathe.outvoice.organization.OrganizationRepository;
import com.omkarsathe.outvoice.security.JwtService;
import com.omkarsathe.outvoice.security.UserContext;
import com.omkarsathe.outvoice.user.User;
import com.omkarsathe.outvoice.user.UserRepository;
import com.omkarsathe.outvoice.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrgUserRepository orgUserRepository;
    private final OrgInvitationRepository invitationRepository;
    private final OrganizationRepository orgRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.allow-duplicate-signup:false}")
    private boolean allowDuplicateSignup;

    /**
     * Creates a new user account with a default personal organization.
     * Returns a fully scoped org JWT so the user can immediately access the app.
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (allowDuplicateSignup) {
            String emailParam = StringUtils.hasText(request.getEmail()) ? request.getEmail() : "";
            String mobileParam = StringUtils.hasText(request.getMobileNumber()) ? request.getMobileNumber() : "";
            var existing = userRepository.findByEmailOrMobileNumber(emailParam, mobileParam);
            if (existing.isPresent()) {
                return new AuthResponse(jwtService.generateUserToken(existing.get().getId()));
            }
        }

        User user = userRepository.save(User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build());

        String orgName = StringUtils.hasText(request.getFullName())
                ? request.getFullName() + "'s Organization"
                : "My Organization";
        Organization org = orgRepository.save(Organization.builder()
                .name(orgName)
                .slug(generateUniqueSlug(orgName))
                .owner(user)
                .build());

        OrgUser membership = orgUserRepository.save(OrgUser.builder()
                .org(org)
                .user(user)
                .role(OrgRole.OWNER)
                .status(OrgUserStatus.ACTIVE)
                .build());

        String token = jwtService.generateOrgToken(
                user.getId(),
                org.getId(),
                membership.getRole(),
                membership.getPermissions());

        return new AuthResponse(token);
    }

    private String generateUniqueSlug(String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (!orgRepository.existsBySlug(base)) {
            return base;
        }
        SecureRandom rng = new SecureRandom();
        String candidate;
        do {
            candidate = base + "-" + String.format("%06x", rng.nextInt(0x1000000));
        } while (orgRepository.existsBySlug(candidate));
        return candidate;
    }

    /**
     * Authenticates a user and returns:
     * - A fully scoped JWT if the user belongs to exactly one org.
     * - A user-level JWT + org list if the user belongs to multiple orgs (triggers org picker).
     * - A user-level JWT + empty list if the user has no org yet (new invited user).
     */
    public AuthResponse login(LoginRequest request) {
        String id = request.getIdentifier();
        User user = userRepository.findByEmailOrMobileNumber(id, id)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BadCredentialsException("Account is suspended");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        List<OrgUser> memberships = orgUserRepository.findByUserIdAndStatus(user.getId(), OrgUserStatus.ACTIVE);

        if (memberships.size() == 1) {
            // Auto-issue scoped token for single-org users
            OrgUser membership = memberships.get(0);
            String token = jwtService.generateOrgToken(
                    user.getId(),
                    membership.getOrg().getId(),
                    membership.getRole(),
                    membership.getPermissions());
            return new AuthResponse(token);
        }

        // Multi-org or no-org: return user-level token + org list for picker
        String userToken = jwtService.generateUserToken(user.getId());
        List<OrgSummaryDto> orgList = memberships.stream()
                .map(m -> new OrgSummaryDto(
                        m.getOrg().getId(),
                        m.getOrg().getName(),
                        m.getOrg().getSlug(),
                        m.getRole()))
                .toList();
        return new AuthResponse(userToken, orgList);
    }

    /**
     * Exchanges a user-level JWT + selected orgId for a fully scoped org JWT.
     * The UserContext in the SecurityContext is the user-level context set by JwtAuthFilter.
     */
    @Transactional(readOnly = true)
    public AuthResponse selectOrg(UserContext currentUser, SelectOrgRequest request) {
        OrgUser membership = orgUserRepository
                .findActiveByOrgIdAndUserId(request.getOrgId(), currentUser.userId())
                .orElseThrow(() -> new BadCredentialsException("User is not an active member of this org"));

        String token = jwtService.generateOrgToken(
                currentUser.userId(),
                membership.getOrg().getId(),
                membership.getRole(),
                membership.getPermissions());

        return new AuthResponse(token);
    }

    /**
     * Accepts an org invitation by secure token.
     * - If the invitee already has an account, links them to the org.
     * - If not, creates the user then links them.
     */
    @Transactional
    public AuthResponse acceptInvite(AcceptInviteRequest request) {
        OrgInvitation invitation = invitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid or expired invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("This invitation has already been " + invitation.getStatus().name().toLowerCase());
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BusinessException("This invitation has expired");
        }

        // Find or create the invitee's user account
        String inviteeIdentifier = invitation.getInviteeEmail() != null
                ? invitation.getInviteeEmail()
                : invitation.getInviteeMobile();

        User user = userRepository.findByEmailOrMobileNumber(inviteeIdentifier, inviteeIdentifier)
                .orElseGet(() -> {
                    if (!StringUtils.hasText(request.getPassword())) {
                        throw new BusinessException("Password is required to create a new account");
                    }
                    return userRepository.save(User.builder()
                            .fullName(request.getFullName())
                            .email(invitation.getInviteeEmail())
                            .mobileNumber(invitation.getInviteeMobile())
                            .passwordHash(passwordEncoder.encode(request.getPassword()))
                            .build());
                });

        // Create the org membership
        OrgUser membership = OrgUser.builder()
                .org(invitation.getOrg())
                .user(user)
                .role(invitation.getIntendedRole())
                .permissions(invitation.getIntendedPermissions())
                .invitedBy(invitation.getInvitedBy())
                .status(OrgUserStatus.ACTIVE)
                .build();
        orgUserRepository.save(membership);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        // Issue a scoped token for the newly joined org
        String token = jwtService.generateOrgToken(
                user.getId(),
                invitation.getOrg().getId(),
                membership.getRole(),
                membership.getPermissions());

        return new AuthResponse(token);
    }
}
