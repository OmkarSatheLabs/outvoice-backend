//package com.omkarsathe.outvoice.workspace.customer;
//
//import com.omkarsathe.outvoice.country.Country;
//import com.omkarsathe.outvoice.country.CountryRepository;
//import com.omkarsathe.outvoice.country.CountryService;
//import com.omkarsathe.outvoice.currency.CurrencyEntity;
//import com.omkarsathe.outvoice.currency.CurrencyRepository;
//import com.omkarsathe.outvoice.currency.CurrencyService;
//import com.omkarsathe.outvoice.phone.PhoneCode;
//import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
//import com.omkarsathe.outvoice.phone.PhoneCodeService;
//import com.omkarsathe.outvoice.user.UserEntity;
//import com.omkarsathe.outvoice.user.UserRepository;
//import com.omkarsathe.outvoice.user.UserService;
//import com.omkarsathe.outvoice.user.dto.CreateUserRequestDto;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceService;
//import com.omkarsathe.outvoice.user.workspace.dto.CreateUserWorkspaceRequestDto;
//import com.omkarsathe.outvoice.workspace.*;
//import com.omkarsathe.outvoice.workspace.customer.dto.*;
//import com.omkarsathe.outvoice.workspace.dto.CreateWorkspaceRequestDto;
////import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
////import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.server.ResponseStatusException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class WorkspaceCustomerService {
//
//    private final WorkspaceService workspaceService;
//    private final UserService userService;
//    private final UserWorkspaceService userWorkspaceService;
//    private final PhoneCodeService phoneCodeService;
//    private final CountryService countryService;
//    private final CurrencyService currencyService;
////    private final WorkspaceRepository workspaceRepository;
//    private final UserRepository userRepository;
//    private final UserWorkspaceRepository userWorkspaceRepository;
//    private final PhoneCodeRepository phoneCodeRepository;
//    private final CountryRepository countryRepository;
//    private final CurrencyRepository currencyRepository;
//    private final WorkspaceCustomerRepository workspaceCustomerRepository;
//
//    @Transactional(readOnly = true)
//    public List<CustomerResponse> getCustomersByWorkspaceId(String id) {
//        UUID workspaceId = UUID.fromString(id);
//        return workspaceCustomerRepository.findByWorkspaceId(workspaceId).stream()
//                .map(customer -> CustomerResponse.builder()
//                        .id(customer.getId())
//                        .name(customer.getDisplayName())
//                        .email(customer.getEmail())
//                        .mobile(customer.getMobile())
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<SearchEntityResponse> searchEntities(String id, SearchQueryTypeEnum queryType, String emailOrPhoneCodeId, String mobile) {
//        if (!StringUtils.hasText(emailOrPhoneCodeId) || emailOrPhoneCodeId.trim().length() < 2) {
//            return new ArrayList<>();
//        }
//
//        UUID workspaceId = UUID.fromString(id);
//
//        // Find existing customers in this workspace to filter out
//        List<WorkspaceCustomerEntity> existingCustomers = workspaceCustomerRepository.findByWorkspaceId(workspaceId);
////        Set<UUID> existingCustomerUserIds = existingCustomers.stream()
////                .map(WorkspaceCustomerEntity::getWorkspace)
////                .filter(Objects::nonNull)
////                .map(UserEntity::getId)
////                .collect(Collectors.toSet());
//
//        // Find associated users in this workspace to filter out
//        List<UserWorkspaceEntity> associatedMembers = userWorkspaceRepository.findByWorkspaceId(workspaceId);
//        Set<UUID> associatedUserIds = associatedMembers.stream()
//                .map(UserWorkspaceEntity::getUser)
//                .filter(Objects::nonNull)
//                .map(UserEntity::getId)
//                .collect(Collectors.toSet());
//
//        switch (queryType) {
//            case EMAIL -> {
//                String cleanEmail = emailOrPhoneCodeId.trim();
//
//                List<UserEntity> users = userRepository.searchUsers(cleanEmail);
//
//                return users.stream()
//                        .map(user -> SearchEntityResponse.builder()
//                                .id(user.getId())
//                                .customerName(user.getFullName())
//                                .workspaces(
//                                        user.getWorkspaces().stream()
//                                                .map(ws -> SearchWorkspaceResponse.builder()
//                                                        .id(ws.getId())
//                                                        .workspaceName(ws.getName())
//                                                        .countryId(ws.getCountry() != null ? ws.getCountry().getId() : null)
//                                                        .currencyId(ws.getCurrency() != null ? ws.getCurrency().getId() : null)
//                                                        .build())
//                                                .toList()
//                                )
//                                .email(user.getEmail())
//                                .phoneCodeId(user.getPhoneCode() != null ? user.getPhoneCode().getId() : null)
//                                .mobile(maskMobile(user.getMobile()))
//                                .build())
//                        .toList();
//            }
//
//            case MOBILE -> {
//                UUID cleanPhoneCodeId = UUID.fromString(emailOrPhoneCodeId.trim());
//                String cleanMobile = mobile.trim();
//
//                List<UserEntity> users = userRepository.findAllByPhoneCodeIdAndMobileContaining(cleanPhoneCodeId, cleanMobile);
//
//                return users.stream()
//                        .map(user -> SearchEntityResponse.builder()
//                                .id(user.getId())
//                                .customerName(user.getFullName())
//                                .workspaces(
//                                        user.getWorkspaces().stream()
//                                                .map(ws -> SearchWorkspaceResponse.builder()
//                                                        .id(ws.getId())
//                                                        .workspaceName(ws.getName())
//                                                        .countryId(ws.getCountry() != null ? ws.getCountry().getId() : null)
//                                                        .currencyId(ws.getCurrency() != null ? ws.getCurrency().getId() : null)
//                                                        .build())
//                                                .toList()
//                                )
//                                .email(maskEmail(user.getEmail()))
//                                .phoneCodeId(user.getPhoneCode() != null ? user.getPhoneCode().getId() : null)
//                                .mobile(user.getMobile())
//                                .build())
//                        .toList();
//            }
//        }
//        return new ArrayList<>();
//    }
//
//    private String maskMobile(String mobile) {
//        if (!StringUtils.hasText(mobile)) {
//            return mobile;
//        }
//        String trimmed = mobile.trim();
//        if (trimmed.length() <= 4) {
//            return "*".repeat(trimmed.length());
//        }
//        return "*".repeat(trimmed.length() - 4) + trimmed.substring(trimmed.length() - 4);
//    }
//
//    private String maskEmail(String email) {
//        if (!StringUtils.hasText(email)) {
//            return email;
//        }
//        String trimmed = email.trim();
//        int atIndex = trimmed.indexOf('@');
//        if (atIndex <= 0) {
//            return trimmed;
//        }
//        String local = trimmed.substring(0, atIndex);
//        String domain = trimmed.substring(atIndex);
//
//        String maskedLocal;
//        if (local.length() <= 3) {
//            maskedLocal = local.substring(0, 1) + "*".repeat(local.length() - 1);
//        } else {
//            maskedLocal = local.substring(0, 3) + "*".repeat(local.length() - 3);
//        }
//
//        return maskedLocal + domain;
//    }
//
//    @Transactional
//    public CreateWorkspaceCustomerResponseDto createWorkspaceCustomer(UUID workspaceId, CreateCustomerRequest request, UUID createdById) {
//
//        if (request.getIdentityResolution() == null ||
//            request.getIdentityResolution().getIdentityMatch() == null ||
//            request.getIdentityResolution().getIdentityMatch().getStatus() == null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identity resolution and match status must be provided");
//        }
//
//        IdentityMatchStatus identityMatchStatus = request.getIdentityResolution().getIdentityMatch().getStatus();
//        SearchQueryTypeEnum identityMatchType = request.getIdentityResolution().getIdentityMatch().getType();
//
//        switch (identityMatchStatus) {
//            case MATCHED -> {
//                switch (identityMatchType) {
//                    case EMAIL -> {
//                        // Case where there is match with email
//                        // We receive user email and userId
//                        // Validate if the email belongs to the same user with userId
//                        String customerEmail = request.getEmail();
//                        UUID customerId = request.getIdentityResolution().getIdentityMatch().getCustomerId();
//                        UUID customerWorkspaceId = request.getIdentityResolution().getIdentityMatch().getWorkspaceId();
//
//                        userService.assertEmailBelongsToUser(customerId, customerEmail);
//
//                        // Validate if the user id and workspace id are related
//                        userWorkspaceService.assertUserBelongsToWorkspace(customerId, customerWorkspaceId);
//
//                        // Create an association between two workspaces. workspace -> customer (workspace)
//                        Workspace billingEntity = workspaceService.findById(workspaceId);
//                        Workspace billedEntity = workspaceService.findById(customerWorkspaceId);
//                        UserEntity accountsPayable = userService.findById(customerId);
//
//                        WorkspaceCustomerEntity workspaceCustomer = new WorkspaceCustomerEntity();
//
//                        workspaceCustomer.setWorkspace(billingEntity);
//                        workspaceCustomer.setDisplayName(billedEntity.getName());
//                        workspaceCustomer.setEmail(accountsPayable.getEmail());
//                        workspaceCustomer.setLinkedWorkspace(billedEntity);
//                        workspaceCustomer.setDefaultCurrency(billedEntity.getBaseCurrency());
//
//                        WorkspaceCustomerEntity saved = workspaceCustomerRepository.save(workspaceCustomer);
//
//                        return CreateWorkspaceCustomerResponseDto.builder()
//                                .id(saved.getId())
//                                .workspaceId(saved.getWorkspace().getId())
//                                .displayName(saved.getDisplayName())
//                                .email(saved.getEmail())
//                                .mobile(saved.getMobile())
//                                .linkedWorkspaceId(saved.getLinkedWorkspace().getId())
//                                .defaultCurrencyId(saved.getDefaultCurrency())
//                                .build();
//                    }
//
//                    case MOBILE -> {
//                        UUID phoneCodeId = request.getPhoneCodeId();
//                        String mobile = request.getMobile();
//                        UUID customerId = request.getIdentityResolution().getIdentityMatch().getCustomerId();
//                        UUID customerWorkspaceId = request.getIdentityResolution().getIdentityMatch().getWorkspaceId();
//
//                        // Case where there is match with phone code id + mobile number
//                        // We receive user phone code id + mobile number and userId
//                        // Validate if the phone code id + mobile number belongs to the same user with userId
//                        userService.assertMobileBelongsToUser(customerId, phoneCodeId, mobile);
//
//                        // Validate if the user id and workspace id are related
//                        userWorkspaceService.assertUserBelongsToWorkspace(customerId, customerWorkspaceId);
//
//                        // Create an association between two workspaces. workspace -> customer (workspace)
//                        Workspace billingEntity = workspaceService.findById(workspaceId);
//                        Workspace billedEntity = workspaceService.findById(customerWorkspaceId);
//                        UserEntity accountsPayable = userService.findById(customerId);
//
//                        WorkspaceCustomerEntity workspaceCustomer = new WorkspaceCustomerEntity();
//
//                        workspaceCustomer.setWorkspace(billingEntity);
//                        workspaceCustomer.setDisplayName(billedEntity.getName());
//                        workspaceCustomer.setEmail(accountsPayable.getEmail());
//                        workspaceCustomer.setLinkedWorkspace(billedEntity);
//                        workspaceCustomer.setDefaultCurrency(billedEntity.getBaseCurrency());
//
//                        WorkspaceCustomerEntity saved = workspaceCustomerRepository.save(workspaceCustomer);
//
//                        return CreateWorkspaceCustomerResponseDto.builder()
//                                .id(saved.getId())
//                                .workspaceId(saved.getWorkspace().getId())
//                                .displayName(saved.getDisplayName())
//                                .email(saved.getEmail())
//                                .mobile(saved.getMobile())
//                                .linkedWorkspaceId(saved.getLinkedWorkspace().getId())
//                                .defaultCurrencyId(saved.getDefaultCurrency())
//                                .build();
//                    }
//
//                    default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown identity match type");
//                }
//            }
//
//            case NO_MATCH -> {
//                // proceed to default creation flow below
//            }
//
//            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown identity match status");
//        }
//
//        // When there is no match
//        // Create a new placeholder user + a placeholder workspace + their association
//        // add that placeholder workspace as a customer to the current workspace
//        UserEntity invitedBy = userService.findById(createdById);
//        Workspace billingEntity = workspaceService.findById(workspaceId);
//
//        PhoneCode phoneCode = phoneCodeService.findById(request.getPhoneCodeId());
//        Country country = countryService.findById(request.getCountryId());
//        CurrencyEntity currency = currencyService.findById(request.getCurrencyId());
//
//        CreateUserRequestDto createPlaceholderAccountsPayableRequest = new CreateUserRequestDto();
//
//        createPlaceholderAccountsPayableRequest.setEmail(request.getEmail());
//        createPlaceholderAccountsPayableRequest.setPhoneCode(phoneCode);
//        createPlaceholderAccountsPayableRequest.setMobile(request.getMobile());
//        createPlaceholderAccountsPayableRequest.setFullName(request.getCustomerName());
//        createPlaceholderAccountsPayableRequest.setHashedPassword("$2a$10$UnregisteredPlaceholderHashNoLogInPossibleDirectly");
//        createPlaceholderAccountsPayableRequest.setCountry(country);
//        createPlaceholderAccountsPayableRequest.setIsPlaceholder(true);
//        createPlaceholderAccountsPayableRequest.setInvitedAt(LocalDateTime.now());
//        createPlaceholderAccountsPayableRequest.setInvitedBy(invitedBy);
//
//        UserEntity placeholderAccountsPayable = userService.save(createPlaceholderAccountsPayableRequest);
//
//        CreateWorkspaceRequestDto createPlaceholderBilledWorkspaceRequest = new CreateWorkspaceRequestDto();
//
//        createPlaceholderBilledWorkspaceRequest.setName(request.getCompanyName());
//        createPlaceholderBilledWorkspaceRequest.setCurrency(currency);
//        createPlaceholderBilledWorkspaceRequest.setClaimed(false);
//        createPlaceholderBilledWorkspaceRequest.setCountry(country);
//        createPlaceholderBilledWorkspaceRequest.setSupportEmail(request.getEmail());
//        createPlaceholderBilledWorkspaceRequest.setCreatedBy(null);
//
//        Workspace placeholderBilledWorkspace = workspaceService.save(createPlaceholderBilledWorkspaceRequest);
//
//        CreateUserWorkspaceRequestDto userWorkspaceRequest = new CreateUserWorkspaceRequestDto();
//
//        userWorkspaceRequest.setUser(placeholderAccountsPayable);
//        userWorkspaceRequest.setWorkspace(placeholderBilledWorkspace);
////        userWorkspaceRequest.setRole(null);
//
//        userWorkspaceService.save(userWorkspaceRequest);
//
//        WorkspaceCustomerEntity workspaceCustomer = new WorkspaceCustomerEntity();
//
//        workspaceCustomer.setWorkspace(billingEntity);
//        workspaceCustomer.setDisplayName(placeholderBilledWorkspace.getName());
//        workspaceCustomer.setEmail(placeholderAccountsPayable.getEmail());
//        workspaceCustomer.setMobile(placeholderAccountsPayable.getMobile());
//        workspaceCustomer.setLinkedWorkspace(placeholderBilledWorkspace);
//        workspaceCustomer.setDefaultCurrency(placeholderBilledWorkspace.getBaseCurrency());
//
//        WorkspaceCustomerEntity saved = workspaceCustomerRepository.save(workspaceCustomer);
//
//        return CreateWorkspaceCustomerResponseDto.builder()
//                .id(saved.getId())
//                .workspaceId(saved.getWorkspace().getId())
//                .displayName(saved.getDisplayName())
//                .email(saved.getEmail())
//                .mobile(saved.getMobile())
//                .linkedWorkspaceId(saved.getLinkedWorkspace().getId())
//                .defaultCurrencyId(saved.getDefaultCurrency())
//                .build();
//
////        UserEntity invitedBy = userRepository.findById(createdById)
////                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
////
////        PhoneCode phoneCode = request.getPhoneCodeId() != null
////                ? phoneCodeRepository.findById(request.getPhoneCodeId()).orElse(null)
////                : null;
////
////        Country country = request.getCountryId() != null
////                ? countryRepository.findById(request.getCountryId()).orElse(billingWorkspace.getCountry())
////                : billingWorkspace.getCountry();
////
////        CurrencyEntity currency = request.getCurrencyId() != null
////                ? currencyRepository.findById(request.getCurrencyId()).orElse(billingWorkspace.getCurrency())
////                : billingWorkspace.getCurrency();
////
////        String slug = generateUniqueSlug(request.getCompanyName());
////
////        UserEntity clientUser = null;
////        Workspace clientWorkspace = null;
////
////        // 1. Resolve Client UserEntity (Person)
////        if (request.getIdentityResolution() != null &&
////            request.getIdentityResolution().getIdentityMatch() != null &&
////            request.getIdentityResolution().getIdentityMatch().getCustomerId() != null) {
////            clientUser = userRepository.findById(request.getIdentityResolution().getIdentityMatch().getCustomerId()).orElse(null);
////        }
////
////        if (clientUser == null && StringUtils.hasText(request.getEmail())) {
////            clientUser = userRepository.findByEmail(request.getEmail().trim()).orElse(null);
////        }
////
////        if (clientUser == null && StringUtils.hasText(request.getMobile()) && request.getPhoneCodeId() != null) {
////            clientUser = userRepository.findByMobileAndPhoneCodeId(request.getMobile().trim(), request.getPhoneCodeId()).orElse(null);
////        }
////
////        if (clientUser == null) {
////            // Create a placeholder UserEntity
////            clientUser = userRepository.save(UserEntity.builder()
////                    .fullName(request.getCustomerName().trim())
////                    .email(request.getEmail() != null ? request.getEmail().trim() : null)
////                    .phoneCode(phoneCode)
////                    .mobile(StringUtils.hasText(request.getMobile()) ? request.getMobile() : null)
////                    .isPlaceholder(true)
////                    .invitedAt(LocalDateTime.now())
////                    .invitedBy(invitedBy)
////                    // Unusable password hash for placeholder users
////                    .passwordHash("$2a$10$UnregisteredPlaceholderHashNoLogInPossibleDirectly")
////                    .isEmailVerified(false)
////                    .isMobileVerified(false)
////                    .country(country)
////                    .build());
////        }
////
////        // 2. Resolve Client Workspace (Company)
////        if (request.getIdentityResolution() != null &&
////            request.getIdentityResolution().getIdentityMatch() != null &&
////            request.getIdentityResolution().getIdentityMatch().getWorkspaceId() != null) {
////            clientWorkspace = workspaceService.findById(request.getIdentityResolution().getIdentityMatch().getWorkspaceId());
////        }
////
////        if (clientWorkspace == null && StringUtils.hasText(request.getCompanyName())) {
////            CreateWorkspaceRequestDto workspaceRequest = new CreateWorkspaceRequestDto();
////            workspaceRequest.setName(request.getCompanyName().trim());
////            workspaceRequest.setSlug(slug);
////            workspaceRequest.setCountry(country);
////            workspaceRequest.setCurrency(currency);
////            workspaceRequest.setCreatedBy(clientUser);
////
////            workspaceService.save(workspaceRequest);
////
////            // Create Member Relationship
////            userWorkspaceRepository.save(UserWorkspaceEntity.builder()
////                    .user(clientUser)
////                    .workspace(clientWorkspace)
////                    .role(WorkspaceRole.OWNER)
////                    .isDefaultWorkspace(true)
////                    .status(MemberStatusEnum.ACTIVE)
////                    .build());
////        }
////
////        // 3. Save WorkspaceCustomerEntity Directory Relationship
////        WorkspaceCustomerEntity workspaceCustomer = workspaceCustomerRepository.save(WorkspaceCustomerEntity.builder()
////                .displayName(request.getCustomerName().trim())
////                .email(request.getEmail() != null ? request.getEmail().trim() : null)
////                .mobile(request.getMobile())
////                .workspace(billingWorkspace)
////                .defaultCurrency(currency.getCode())
////                .build());
////
////        WorkspaceCustomerEntity workspaceCustomer = new WorkspaceCustomerEntity();
////
////        workspaceCustomer.setWorkspace(billingEntity);
////        workspaceCustomer.setDisplayName(billedEntity.getName());
////        workspaceCustomer.setEmail(accountsPayable.getEmail());
////        workspaceCustomer.setLinkedWorkspace(billedEntity);
////        workspaceCustomer.setDefaultCurrency(billedEntity.getBaseCurrency());
////
////        WorkspaceCustomerEntity saved = workspaceCustomerRepository.save(workspaceCustomer);
////
////        return CreateWorkspaceCustomerResponseDto.builder()
////                .id(saved.getId())
////                .workspace(saved.getWorkspace())
////                .displayName(saved.getDisplayName())
////                .email(saved.getEmail())
////                .mobile(saved.getMobile())
////                .linkedWorkspace(saved.getLinkedWorkspace())
////                .defaultCurrency(saved.getDefaultCurrency())
////                .build();
//    }
//
//}
