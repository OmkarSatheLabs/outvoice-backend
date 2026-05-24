package com.omkarsathe.outvoice.auth;

import com.omkarsathe.outvoice.auth.dto.AuthResponse;
import com.omkarsathe.outvoice.auth.dto.LoginRequest;
import com.omkarsathe.outvoice.auth.dto.SignupRequest;
import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.country.CountryRepository;
import com.omkarsathe.outvoice.currency.Currency;
import com.omkarsathe.outvoice.currency.CurrencyRepository;
import com.omkarsathe.outvoice.organization.MemberStatus;
import com.omkarsathe.outvoice.organization.OrgRole;
import com.omkarsathe.outvoice.organization.Organization;
import com.omkarsathe.outvoice.organization.OrganizationRepository;
import com.omkarsathe.outvoice.organization.UserOrganization;
import com.omkarsathe.outvoice.organization.UserOrganizationRepository;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
import com.omkarsathe.outvoice.security.JwtService;
import com.omkarsathe.outvoice.user.User;
import com.omkarsathe.outvoice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
    private final PhoneCodeRepository phoneCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.allow-duplicate-signup:false}")
    private boolean allowDuplicateSignup;

    @Transactional
    public AuthResponse signup(SignupRequest request) {

        if (userRepository.findByEmailOrMobileIfPresent(request.getEmail(), request.getMobile()).isPresent()) {
            throw new BadCredentialsException("An account with this email or mobile number already exists.");
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

        Country organizationCountry = countryRepository.findById(
                request.getOrganizationCountryId() != null
                        ? request.getOrganizationCountryId()
                        : request.getUserCountryId()
        ).orElseThrow(() -> new RuntimeException("Country not found"));

        Currency currency = currencyRepository.findById(
                request.getCurrencyId()
        ).orElseThrow(()  -> new RuntimeException("Currency not found"));

        Organization org = organizationRepository.save(Organization.builder()
                .name(request.getOrganizationName())
                .slug(request.getOrganizationSlug())
                .country(organizationCountry)
                .currency(currency)
                .taxComplianceName(taxComplianceName)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        userOrganizationRepository.save(UserOrganization.builder()
                .user(user)
                .org(org)
                .role(OrgRole.OWNER)
                .isDefaultOrg(true)
                .status(MemberStatus.ACTIVE)
                .build());

        return new AuthResponse(jwtService.generateToken(user.getPrincipal()));
    }

    public AuthResponse login(LoginRequest request) {
        String id = request.getIdentifier();
        User user = userRepository.findByEmailOrMobileIfPresent(id, id)
                .orElseThrow(() -> new BadCredentialsException("Invalid email/mobile or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email/mobile or password.");
        }

        return new AuthResponse(jwtService.generateToken(user.getPrincipal()));
    }
}
