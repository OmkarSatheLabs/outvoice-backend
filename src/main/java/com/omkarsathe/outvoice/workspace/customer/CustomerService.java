package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.country.CountryRepository;
import com.omkarsathe.outvoice.currency.Currency;
import com.omkarsathe.outvoice.currency.CurrencyRepository;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.user.UserRepository;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
import com.omkarsathe.outvoice.workspace.*;
import com.omkarsathe.outvoice.workspace.customer.dto.CreateCustomerRequest;
import com.omkarsathe.outvoice.workspace.customer.dto.CustomerResponse;
import com.omkarsathe.outvoice.workspace.customer.dto.SearchEntityResponse;
import com.omkarsathe.outvoice.workspace.customer.dto.SearchWorkspaceResponse;
import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final UserWorkspaceRepository userWorkspaceRepository;
    private final PhoneCodeRepository phoneCodeRepository;
    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomers(String id) {
        UUID workspaceId = UUID.fromString(id);
        return customerRepository.findByWorkspaceId(workspaceId).stream()
                .map(customer -> CustomerResponse.builder()
                        .id(customer.getId())
                        .name(customer.getCustomerName())
                        .email(customer.getEmail())
                        .phoneCodeId(customer.getPhoneCodeId() != null ? customer.getPhoneCodeId().getId() : null)
                        .phoneCode(customer.getPhoneCodeId() != null ? customer.getPhoneCodeId().getCode() : null)
                        .mobile(customer.getMobile())
                        .companyName(customer.getCompanyName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SearchEntityResponse> searchEntities(String id, SearchQueryTypeEnum queryType, String emailOrPhoneCodeId, String mobile) {
        if (!StringUtils.hasText(emailOrPhoneCodeId) || emailOrPhoneCodeId.trim().length() < 2) {
            return new ArrayList<>();
        }

        UUID workspaceId = UUID.fromString(id);

        // Find existing customers in this workspace to filter out
        List<CustomerEntity> existingCustomers = customerRepository.findByWorkspaceId(workspaceId);
        Set<UUID> existingCustomerUserIds = existingCustomers.stream()
                .map(CustomerEntity::getUser)
                .filter(Objects::nonNull)
                .map(UserEntity::getId)
                .collect(Collectors.toSet());

        // Find associated users in this workspace to filter out
        List<UserWorkspaceEntity> associatedMembers = userWorkspaceRepository.findByWorkspaceId(workspaceId);
        Set<UUID> associatedUserIds = associatedMembers.stream()
                .map(UserWorkspaceEntity::getUser)
                .filter(Objects::nonNull)
                .map(UserEntity::getId)
                .collect(Collectors.toSet());

        switch (queryType) {
            case EMAIL -> {
                String cleanEmail = emailOrPhoneCodeId.trim();

                List<UserEntity> users = userRepository.searchUsers(cleanEmail);

                return users.stream()
                        .filter(user -> !existingCustomerUserIds.contains(user.getId()) && !associatedUserIds.contains(user.getId()))
                        .map(user -> SearchEntityResponse.builder()
                                .id(user.getId())
                                .customerName(user.getFullName())
                                .workspaces(
                                        user.getWorkspaces().stream()
                                                .map(ws -> SearchWorkspaceResponse.builder()
                                                        .id(ws.getId())
                                                        .workspaceName(ws.getName())
                                                        .countryId(ws.getCountry() != null ? ws.getCountry().getId() : null)
                                                        .currencyId(ws.getCurrency() != null ? ws.getCurrency().getId() : null)
                                                        .build())
                                                .toList()
                                )
                                .email(user.getEmail())
                                .phoneCodeId(user.getPhoneCode() != null ? user.getPhoneCode().getId() : null)
                                .mobile(maskMobile(user.getMobile()))
                                .build())
                        .toList();
            }

            case MOBILE -> {
                UUID cleanPhoneCodeId = UUID.fromString(emailOrPhoneCodeId.trim());
                String cleanMobile = mobile.trim();

                List<UserEntity> users = userRepository.findAllByPhoneCodeIdAndMobileContaining(cleanPhoneCodeId, cleanMobile);

                return users.stream()
                        .filter(user -> !existingCustomerUserIds.contains(user.getId()) && !associatedUserIds.contains(user.getId()))
                        .map(user -> SearchEntityResponse.builder()
                                .id(user.getId())
                                .customerName(user.getFullName())
                                .workspaces(
                                        user.getWorkspaces().stream()
                                                .map(ws -> SearchWorkspaceResponse.builder()
                                                        .id(ws.getId())
                                                        .workspaceName(ws.getName())
                                                        .countryId(ws.getCountry() != null ? ws.getCountry().getId() : null)
                                                        .currencyId(ws.getCurrency() != null ? ws.getCurrency().getId() : null)
                                                        .build())
                                                .toList()
                                )
                                .email(maskEmail(user.getEmail()))
                                .phoneCodeId(user.getPhoneCode() != null ? user.getPhoneCode().getId() : null)
                                .mobile(user.getMobile())
                                .build())
                        .toList();
            }
        }
        return new ArrayList<>();
    }

    private String maskMobile(String mobile) {
        if (!StringUtils.hasText(mobile)) {
            return mobile;
        }
        String trimmed = mobile.trim();
        if (trimmed.length() <= 4) {
            return "*".repeat(trimmed.length());
        }
        return "*".repeat(trimmed.length() - 4) + trimmed.substring(trimmed.length() - 4);
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0) {
            return trimmed;
        }
        String local = trimmed.substring(0, atIndex);
        String domain = trimmed.substring(atIndex);
        
        String maskedLocal;
        if (local.length() <= 3) {
            maskedLocal = local.substring(0, 1) + "*".repeat(local.length() - 1);
        } else {
            maskedLocal = local.substring(0, 3) + "*".repeat(local.length() - 3);
        }
        
        return maskedLocal + domain;
    }

    @Transactional
    public CustomerResponse createCustomer(String workspaceIdStr, CreateCustomerRequest request, UUID userId) {
        UUID workspaceId = UUID.fromString(workspaceIdStr);
        WorkspaceEntity billingWorkspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Billing workspace not found"));

        // Check if the user is already present in the platform
        // Several possible cases:
        // 1. Email address has an associated user
        // 2. Mobile number has an associated user
        // 3. Both email address and mobile number are associated with a single person
        // 4. Both email address and mobile number are associated with the different persons
        // 5. User is present but does not have a default workspace
        // 6. User is present but there are multiple workspaces for which he is owner

        Optional<UserEntity> existingUser = Optional.empty();

        if (request.getEmail() != null) {
            existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {

            }
        }

        UserEntity invitedBy = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        PhoneCode phoneCode = request.getPhoneCodeId() != null
                ? phoneCodeRepository.findById(request.getPhoneCodeId()).orElse(null)
                : null;

        Country country = request.getCountryId() != null
                ? countryRepository.findById(request.getCountryId()).orElse(billingWorkspace.getCountry())
                : billingWorkspace.getCountry();

        Currency currency = request.getCurrencyId() != null
                ? currencyRepository.findById(request.getCurrencyId()).orElse(billingWorkspace.getCurrency())
                : billingWorkspace.getCurrency();

        String slug = generateUniqueSlug(request.getCompanyName());

        UserEntity clientUser = null;
        WorkspaceEntity clientWorkspace = null;

        // 1. Resolve Client UserEntity (Person)
        if (request.getUserId() != null) {
            clientUser = userRepository.findById(request.getUserId()).orElse(null);
        }

        if (clientUser == null && StringUtils.hasText(request.getEmail())) {
            clientUser = userRepository.findByEmail(request.getEmail().trim()).orElse(null);
        }

        if (clientUser == null) {
            // Create a placeholder UserEntity
            clientUser = userRepository.save(UserEntity.builder()
                    .fullName(request.getCustomerName().trim())
                    .email(request.getEmail().trim())
                    .phoneCode(phoneCode)
                    .mobile(request.getMobile())
                    .isPlaceholder(true)
                    .invitedAt(LocalDateTime.now())
                    .invitedBy(invitedBy)
                    // Unusable password hash for placeholder users
                    .passwordHash("$2a$10$UnregisteredPlaceholderHashNoLogInPossibleDirectly")
                    .isEmailVerified(false)
                    .isMobileVerified(false)
                    .country(country)
                    .build());
        }

        // 2. Resolve Client WorkspaceEntity (Company)
        if (request.getWorkspaceId() != null) {
            clientWorkspace = workspaceRepository.findById(request.getWorkspaceId()).orElse(null);
        }

        if (clientWorkspace == null && StringUtils.hasText(request.getCompanyName())) {
            clientWorkspace = workspaceRepository.save(WorkspaceEntity.builder()
                    .name(request.getCompanyName().trim())
                    .slug(slug)
                    .country(country)
                    .currency(currency)
                    .taxComplianceName(request.getCompanyName().trim())
                    .status(WorkspaceStatus.ACTIVE)
                    .createdBy(clientUser)
                    .isPlaceholder(true)
                    .build());

            // Create Member Relationship
            userWorkspaceRepository.save(UserWorkspaceEntity.builder()
                    .user(clientUser)
                    .workspace(clientWorkspace)
                    .role(WorkspaceRole.OWNER)
                    .isDefaultWorkspace(true)
                    .status(MemberStatusEnum.ACTIVE)
                    .build());
        }

        // 3. Save CustomerEntity Directory Relationship
//        PhoneCode phoneCode = request.getPhoneCodeId() != null
//                ? phoneCodeRepository.findById(request.getPhoneCodeId()).orElse(null)
//                : null;

        CustomerEntity customer = customerRepository.save(CustomerEntity.builder()
                .customerName(request.getCustomerName().trim())
                .companyName(request.getCompanyName().trim())
                .email(request.getEmail().trim())
                .phoneCodeId(phoneCode)
                .mobile(request.getMobile())
                .user(clientUser)
                .workspace(clientWorkspace)
                .slug(slug)
                .country(country)
                .currency(currency)
                .build());

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getCustomerName())
                .email(customer.getEmail())
                .phoneCodeId(customer.getPhoneCodeId() != null ? customer.getPhoneCodeId().getId() : null)
                .phoneCode(customer.getPhoneCodeId() != null ? customer.getPhoneCodeId().getCode() : null)
                .mobile(customer.getMobile())
                .companyName(customer.getCompanyName())
                .build();
    }

    private String generateUniqueSlug(String companyName) {
        String baseSlug = companyName.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        if (baseSlug.isEmpty()) {
            baseSlug = "company";
        }
        String slug = baseSlug;
        while (workspaceRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + (int)(Math.random() * 9000 + 1000);
        }
        return slug;
    }
}
