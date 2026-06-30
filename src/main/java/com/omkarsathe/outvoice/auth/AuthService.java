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
import com.omkarsathe.outvoice.user.User;
import com.omkarsathe.outvoice.user.UserRepository;
import com.omkarsathe.outvoice.workspace.*;
import com.omkarsathe.outvoice.workspace.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
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

        boolean emailExists = request.getEmail() != null
                && userRepository.findByEmail(request.getEmail()).isPresent();

        if (emailExists) {
            throw new BadCredentialsException("An account with this email already exists.");
        }

        boolean mobileExists = request.getMobile() != null
                && request.getPhoneCodeId() != null
                && userRepository.findByMobileAndPhoneCodeId(request.getMobile(), request.getPhoneCodeId()).isPresent();

        if (mobileExists) {
            throw new BadCredentialsException("An account with this mobile number already exists.");
        }

        PhoneCode phoneCode = request.getPhoneCodeId() != null
                ? phoneCodeRepository.findById(request.getPhoneCodeId()).orElse(null)
                : null;

        Country userCountry = countryRepository.findById(
                request.getUserCountryId()
        ).orElseThrow(() -> new RuntimeException("Country not found"));

        User user = userRepository.save(User.builder()
                .email(request.getEmail())
                .phoneCode(phoneCode)
                .mobile(request.getMobile())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isEmailVerified(false)
                .isMobileVerified(false)
                .country(userCountry)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        String taxComplianceName = StringUtils.hasText(request.getTaxComplianceName())
                ? request.getTaxComplianceName()
                : request.getFullName();

        Workspace probe = new Workspace();
        probe.setSlug(request.getWorkspaceSlug());
        Example<Workspace> example = Example.of(probe);
        Optional<Workspace> ws = workspaceRepository.findOne(example);

        if (ws.isPresent()) {
            throw new BadCredentialsException("An account with this workspace slug already exists.");
        }

        Country workspaceCountry = countryRepository.findById(
                request.getWorkspaceCountryId() != null
                        ? request.getWorkspaceCountryId()
                        : request.getUserCountryId()
        ).orElseThrow(() -> new RuntimeException("Country not found"));

        Currency currency = currencyRepository.findById(
                request.getCurrencyId()
        ).orElseThrow(()  -> new RuntimeException("Currency not found"));

        Workspace workspace = workspaceRepository.save(Workspace.builder()
                .name(request.getWorkspaceName())
                .slug(request.getWorkspaceSlug())
                .country(workspaceCountry)
                .currency(currency)
                .taxComplianceName(taxComplianceName)
                .status(WorkspaceStatus.ACTIVE)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        userWorkspaceRepository.save(UserWorkspace.builder()
                .user(user)
                .workspace(workspace)
                .role(WorkspaceRole.OWNER)
                .isDefaultWorkspace(true)
                .status(MemberStatus.ACTIVE)
                .build());

        return new AuthResponse(jwtService.generateToken(user.getUsername()));
    }

    public AuthResponse login(LoginRequest request) {
        final String errorMessage = "Invalid email address/mobile number or password";

        Optional<User> userOptional = Optional.empty();
        if (request.getEmail() != null && request.getPassword() != null) {
            userOptional = userRepository.findByEmail(request.getEmail());
        } else if (request.getMobile() != null && request.getPhoneCodeId() != null && request.getPassword() != null) {
            userOptional = userRepository.findByMobileAndPhoneCodeId(request.getMobile(), request.getPhoneCodeId());
        } else {
            throw new BadCredentialsException(errorMessage);
        }

        User user = userOptional.orElseThrow(() -> new BadCredentialsException(errorMessage));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException(errorMessage);
        }

        return new AuthResponse(jwtService.generateToken(user.getUsername()));
    }
}
