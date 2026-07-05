package com.omkarsathe.outvoice.auth;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.auth.dto.LoginRequest;
import com.omkarsathe.outvoice.auth.dto.SignupRequest;
import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.country.CountryRepository;
import com.omkarsathe.outvoice.currency.Currency;
import com.omkarsathe.outvoice.currency.CurrencyRepository;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
import com.omkarsathe.outvoice.security.JwtService;
import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.user.UserRepository;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
import com.omkarsathe.outvoice.workspace.*;
import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserWorkspaceRepository userWorkspaceRepository;
    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
    private final PhoneCodeRepository phoneCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Logger logger = Logger.getLogger(AuthService.class.getName());

    @Transactional
    public AuthResponse signup(SignupRequest request) {

        logger.info("signup request: " + request);

        PhoneCode phoneCode = request.getPhoneCodeId() != null
                ? phoneCodeRepository.findById(request.getPhoneCodeId()).orElse(null)
                : null;

        Country userCountry = countryRepository.findById(
                request.getUserCountryId()
        ).orElseThrow(() -> new RuntimeException("Country not found"));

        Optional<UserEntity> existingUserOpt = request.getEmail() != null
                ? userRepository.findByEmail(request.getEmail())
                : Optional.empty();

        UserEntity user;
        if (existingUserOpt.isPresent()) {
            UserEntity existingUser = existingUserOpt.get();
            if (!existingUser.getIsPlaceholder()) {
                throw new BadCredentialsException("An account with this email already exists.");
            }
            // Claim existing placeholder user profile
            existingUser.setFullName(request.getFullName());
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            existingUser.setPhoneCode(phoneCode);
            existingUser.setMobile(request.getMobile());
            existingUser.setIsPlaceholder(false);
            existingUser.setCountry(userCountry);
            user = userRepository.save(existingUser);
        } else {
            boolean mobileExists = request.getMobile() != null
                    && request.getPhoneCodeId() != null
                    && userRepository.findByMobileAndPhoneCodeId(request.getMobile(), request.getPhoneCodeId())
                            .map(u -> !u.getIsPlaceholder()).orElse(false);

            if (mobileExists) {
                throw new BadCredentialsException("An account with this mobile number already exists.");
            }

            user = userRepository.save(UserEntity.builder()
                    .email(request.getEmail())
                    .phoneCode(phoneCode)
                    .mobile(request.getMobile())
                    .fullName(request.getFullName())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .isEmailVerified(false)
                    .isMobileVerified(false)
                    .isPlaceholder(false)
                    .country(userCountry)
                    .build());
        }

        String taxComplianceName = StringUtils.hasText(request.getTaxComplianceName())
                ? request.getTaxComplianceName()
                : request.getFullName();

        Country workspaceCountry = countryRepository.findById(
                request.getWorkspaceCountryId() != null
                        ? request.getWorkspaceCountryId()
                        : request.getUserCountryId()
        ).orElseThrow(() -> new RuntimeException("Country not found"));

        Currency currency = currencyRepository.findById(
                request.getCurrencyId()
        ).orElseThrow(()  -> new RuntimeException("Currency not found"));

        Optional<WorkspaceEntity> existingWsOpt = workspaceRepository.findBySlug(request.getWorkspaceSlug());
        WorkspaceEntity workspace;
        if (existingWsOpt.isPresent()) {
            WorkspaceEntity existingWs = existingWsOpt.get();
            if (!existingWs.getIsPlaceholder()) {
                throw new BadCredentialsException("An account with this workspace slug already exists.");
            }
            // Claim existing placeholder workspace
            existingWs.setName(request.getWorkspaceName());
            existingWs.setCountry(workspaceCountry);
            existingWs.setCurrency(currency);
            existingWs.setTaxComplianceName(taxComplianceName);
            existingWs.setStatus(WorkspaceStatus.ACTIVE);
            existingWs.setCreatedBy(user);
            existingWs.setIsPlaceholder(false);
            workspace = workspaceRepository.save(existingWs);
        } else {
            workspace = workspaceRepository.save(WorkspaceEntity.builder()
                    .name(request.getWorkspaceName())
                    .slug(request.getWorkspaceSlug())
                    .country(workspaceCountry)
                    .currency(currency)
                    .taxComplianceName(taxComplianceName)
                    .status(WorkspaceStatus.ACTIVE)
                    .createdBy(user)
                    .isPlaceholder(false)
                    .build());
        }

        Optional<UserWorkspaceEntity> existingRel = userWorkspaceRepository.findByUserIdAndWorkspaceId(user.getId(), workspace.getId());
        if (existingRel.isPresent()) {
            UserWorkspaceEntity rel = existingRel.get();
            rel.setRole(WorkspaceRole.OWNER);
            rel.setStatus(MemberStatusEnum.ACTIVE);
            rel.setIsDefaultWorkspace(true);
            userWorkspaceRepository.save(rel);
        } else {
            userWorkspaceRepository.save(UserWorkspaceEntity.builder()
                    .user(user)
                    .workspace(workspace)
                    .role(WorkspaceRole.OWNER)
                    .isDefaultWorkspace(true)
                    .status(MemberStatusEnum.ACTIVE)
                    .build());
        }

        return new AuthResponse(jwtService.generateToken(user.getUsername()));
    }

    public AuthResponse login(LoginRequest request) {
        final String errorMessage = "Invalid email address/mobile number or password";

        Optional<UserEntity> userOptional = Optional.empty();

        if (StringUtils.hasText(request.getEmail()) && StringUtils.hasText(request.getPassword())) {
            userOptional = userRepository.findByEmail(request.getEmail());
        } else if (request.getPhoneCodeId() != null && StringUtils.hasText(request.getMobile())) {
            userOptional = userRepository.findByMobileAndPhoneCodeId(request.getMobile(), request.getPhoneCodeId());
        } else {
            throw new BadCredentialsException(errorMessage);
        }

        UserEntity user = userOptional.orElseThrow(() -> new BadCredentialsException(errorMessage));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException(errorMessage);
        }

        return new AuthResponse(jwtService.generateToken(user.getUsername()));
    }
}
