package com.omkarsathe.outvoice.platform;

import com.omkarsathe.outvoice.common.exception.BusinessException;
import com.omkarsathe.outvoice.common.exception.NotFoundException;
import com.omkarsathe.outvoice.organization.*;
import com.omkarsathe.outvoice.platform.dto.CreatePlatformOrgRequest;
import com.omkarsathe.outvoice.platform.dto.PlatformOrgResponse;
import com.omkarsathe.outvoice.user.User;
import com.omkarsathe.outvoice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/** Platform-admin operations on organisations. */
@Service
@RequiredArgsConstructor
public class PlatformOrgService {

    private final OrganizationRepository orgRepository;
    private final OrgUserRepository orgUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PlatformOrgResponse createOrg(CreatePlatformOrgRequest request) {
        if (orgRepository.existsBySlug(request.getSlug())) {
            throw new BusinessException("Slug '" + request.getSlug() + "' is already taken");
        }

        String identifier = request.getOwnerIdentifier();
        User owner = userRepository.findByEmailOrMobileNumber(identifier, identifier)
                .orElseGet(() -> {
                    if (!request.isCreateOwnerIfNotExists()) {
                        throw new NotFoundException("No user found with identifier: " + identifier);
                    }
                    // Create a user with a temporary random password
                    String tempPassword = UUID.randomUUID().toString();
                    return userRepository.save(User.builder()
                            .fullName(request.getOwnerFullName())
                            .email(looksLikeEmail(identifier) ? identifier : null)
                            .mobileNumber(looksLikeEmail(identifier) ? null : identifier)
                            .passwordHash(passwordEncoder.encode(tempPassword))
                            .build());
                });

        Organization org = orgRepository.save(Organization.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .owner(owner)
                .build());

        orgUserRepository.save(OrgUser.builder()
                .org(org)
                .user(owner)
                .role(OrgRole.OWNER)
                .status(OrgUserStatus.ACTIVE)
                .build());

        return toPlatformOrgResponse(org, 1L);
    }

    @Transactional(readOnly = true)
    public List<PlatformOrgResponse> listOrgs() {
        return orgRepository.findAll().stream()
                .map(org -> {
                    long count = orgUserRepository.findByOrgIdAndStatus(
                            org.getId(), OrgUserStatus.ACTIVE).size();
                    return toPlatformOrgResponse(org, count);
                })
                .toList();
    }

    @Transactional
    public PlatformOrgResponse suspendOrg(UUID orgId) {
        Organization org = loadOrg(orgId);
        if (org.getStatus() == OrgStatus.SUSPENDED) {
            throw new BusinessException("Organisation is already suspended");
        }
        org.setStatus(OrgStatus.SUSPENDED);
        orgRepository.save(org);
        return toPlatformOrgResponse(org, countActiveMembers(orgId));
    }

    @Transactional
    public PlatformOrgResponse activateOrg(UUID orgId) {
        Organization org = loadOrg(orgId);
        if (org.getStatus() == OrgStatus.ACTIVE) {
            throw new BusinessException("Organisation is already active");
        }
        org.setStatus(OrgStatus.ACTIVE);
        orgRepository.save(org);
        return toPlatformOrgResponse(org, countActiveMembers(orgId));
    }

    private PlatformOrgResponse toPlatformOrgResponse(Organization org, long memberCount) {
        return PlatformOrgResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .ownerUserId(org.getOwner() != null ? org.getOwner().getId() : null)
                .ownerEmail(org.getOwner() != null ? org.getOwner().getEmail() : null)
                .status(org.getStatus())
                .memberCount(memberCount)
                .createdAt(org.getCreatedAt())
                .build();
    }

    private Organization loadOrg(UUID orgId) {
        return orgRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organisation not found: " + orgId));
    }

    private long countActiveMembers(UUID orgId) {
        return orgUserRepository.findByOrgIdAndStatus(orgId, OrgUserStatus.ACTIVE).size();
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    private boolean looksLikeEmail(String identifier) {
        return EMAIL_PATTERN.matcher(identifier).matches();
    }
}
