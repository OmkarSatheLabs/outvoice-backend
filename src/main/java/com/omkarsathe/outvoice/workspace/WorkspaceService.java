package com.omkarsathe.outvoice.workspace;
//
//import com.omkarsathe.outvoice.country.Country;
//import com.omkarsathe.outvoice.country.CountryRepository;
//import com.omkarsathe.outvoice.currency.CurrencyEntity;
//import com.omkarsathe.outvoice.currency.CurrencyRepository;
//import com.omkarsathe.outvoice.user.UserEntity;
//import com.omkarsathe.outvoice.user.UserRepository;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceService;
//import com.omkarsathe.outvoice.workspace.currency.dto.CurrencyResponse;
//import com.omkarsathe.outvoice.workspace.dto.CreateWorkspaceRequestDto;
//import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;

//import com.omkarsathe.outvoice.workspace.member.MemberRepository;
import com.omkarsathe.outvoice.user.workspace.role.RoleNotFoundException;
import com.omkarsathe.outvoice.workspace.member.MemberRepository;
import com.omkarsathe.outvoice.workspace.member.MemberResponse;
import com.omkarsathe.outvoice.workspace.user.UserMapper;
import com.omkarsathe.outvoice.workspace.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

////import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//import com.omkarsathe.outvoice.workspace.dto.AdminWorkspaceDto;
//import com.omkarsathe.outvoice.workspace.invoice.WorkspaceInvoiceRepository;
//import com.omkarsathe.outvoice.workspace.invoice.WorkspaceInvoiceEntity;
//import com.omkarsathe.outvoice.workspace.invoice.WorkspaceInvoiceStatus;
//import java.math.BigDecimal;
//
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;
    private final UserMapper userMapper;
    private final WorkspaceResponseMapper workspaceMapper;
    private final UserService userService;

    public Workspace findById(UUID id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Workspace not found: " + id));
    }

    public WorkspaceResponse create(CreateWorkspace request) {
        Workspace workspace = Workspace.builder()
                .name(request.name())
                .build();

        Workspace newWorkspace = workspaceRepository.save(workspace);

        return new WorkspaceResponse(newWorkspace.getId(), newWorkspace.getName());
    }

    @Transactional
    public List<MemberResponse> getMembers(UUID workspaceId) {
        return memberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(member -> new MemberResponse(
                        member.getId(),
                        userMapper.toResponse(member.getUser())
                ))
                .toList();
    }

    public List<WorkspaceResponse> getWorkspaces() {
        return workspaceRepository.findAll()
                .stream()
                .map(workspaceMapper::toResponse)
                .toList();
    }
//
////    private final UserWorkspaceService userWorkspaceService;
////    private final UserRepository userRepository;
////    private final UserWorkspaceRepository userWorkspaceRepository;
////    private final CountryRepository countryRepository;
////    private final CurrencyRepository currencyRepository;
//    private final WorkspaceRepository workspaceRepository;
////    private final WorkspaceInvoiceRepository workspaceInvoiceRepository;
//
//    public WorkspaceResponse create(CreateWorkspace request) {
//        Workspace workspace = Workspace.builder()
//                .name(request.name())
//                .build();
//
//        Workspace newWorkspace = workspaceRepository.save(workspace);
//
//        return new WorkspaceResponse(newWorkspace.getId(), newWorkspace.getName());
//    }
//
////    public boolean existsById(UUID id) {
////        return workspaceRepository.existsById(id);
////    }
////
////    public Optional<Workspace> findBySlug(String slug) {
////        return workspaceRepository.findBySlug(slug);
////    }
////
////    public Workspace findById(UUID id) {
////        return workspaceRepository.findById(id).orElseThrow(() -> new RuntimeException("Workspace with UUID " + id + " not found"));
////    }
////
////    public Workspace save(CreateWorkspaceRequestDto request) {
////
////        Workspace workspace = Workspace.builder()
////                .name(request.getName())
////                .slug(request.getSlug() != null ? request.getSlug() : generateUniqueSlug(request.getName()))
////                .type(request.getType())
////                .baseCurrency(request.getCurrency().getCode())
////                .currency(request.getCurrency())
////                .gstin(request.getGstin())
////                .claimed(request.isClaimed())
////                .country(request.getCountry())
////                .supportEmail(request.getSupportEmail())
////                .createdBy(request.getCreatedBy())
////                .build();
////
////        return workspaceRepository.save(workspace);
////    }
////
////    private String generateUniqueSlug(String companyName) {
////        String baseSlug = companyName.toLowerCase()
////                .replaceAll("[^a-z0-9s-]", "")
////                .replaceAll("\\s+", "-")
////                .replaceAll("-+", "-")
////                .trim();
////        if (baseSlug.isEmpty()) {
////            baseSlug = "company";
////        }
////        String slug = baseSlug;
////        while (this.findBySlug(slug).isPresent()) {
////            slug = baseSlug + "-" + (int)(Math.random() * 9000 + 1000);
////        }
////        return slug;
////    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getUserWorkspaces(UUID userId) {
        return userService.getWorkspaces(userId);
    }

//////    private List<String> buildPermissions(UserWorkspaceEntity uw) {
//////        List<String> permissions = new ArrayList<>(
//////                RolePermissions.forRole(uw.getRole()).stream().map(Enum::name).toList()
//////        );
//////        if (uw.getWorkspace().getType() == WorkspaceType.PLATFORM_ADMIN) {
//////            permissions.addAll(Arrays.stream(PlatformPermission.values()).map(Enum::name).toList());
//////        }
//////        return permissions;
//////    }
////
////    private List<String> buildFeatures(Workspace workspace) {
////        if (workspace.getType() == WorkspaceType.PLATFORM_ADMIN) {
////            return Arrays.stream(Feature.values()).map(Enum::name).toList(); // everything, always
////        }
////        return List.of();
////    }
////
////    public WorkspaceResponse createUserWorkspace(UUID userId, WorkspaceRequest request) {
////        UserEntity createdBy = userRepository.getUserById(userId)
////                .orElseThrow(
////                        () -> new RuntimeException("User with id " + userId + " does not exist")
////                );
////
////        Country country = countryRepository.findById(request.getCountryId())
////                .orElseThrow(
////                        () -> new RuntimeException("Country with id " + request.getCountryId() + " does not exist")
////                );
////
////        CurrencyEntity currency = currencyRepository.findById(request.getCurrencyId())
////                .orElseThrow(
////                        () -> new RuntimeException("Country with id " + request.getCountryId() + " does not exist")
////                );
////
////        Workspace workspace = Workspace.builder()
////                .name(request.getName())
////                .slug(request.getSlug())
////                .country(country)
////                .baseCurrency(currency.getCode())
////                .currency(currency)
////                .status(WorkspaceStatus.ACTIVE)
////                .createdBy(createdBy)
////                .build();
////
////        workspace = workspaceRepository.save(workspace);
////
////        UserWorkspaceEntity userWorkspace = UserWorkspaceEntity.builder()
////                .user(createdBy)
////                .workspace(workspace)
////                .isDefaultWorkspace(false)
////                .invitedBy(null)
////                .joinedAt(null)
////                .status(MemberStatusEnum.ACTIVE)
////                .build();
////
////        userWorkspace = userWorkspaceRepository.save(userWorkspace);
////
////        return WorkspaceResponse.builder()
////                .id(workspace.getId())
////                .name(workspace.getName())
////                .slug(workspace.getSlug())
////                .isDefault(userWorkspace.getIsDefaultWorkspace() != null ? userWorkspace.getIsDefaultWorkspace() : false)
////                .currency(
////                        new CurrencyResponse(
////                                userWorkspace.getWorkspace().getCurrency().getId(),
////                                userWorkspace.getWorkspace().getCurrency().getCode(),
////                                userWorkspace.getWorkspace().getCurrency().getName(),
////                                userWorkspace.getWorkspace().getCurrency().getSymbol(),
////                                userWorkspace.getWorkspace().getCurrency().getDecimalPlaces()
////                        )
////                )
////                .countryId(workspace.getCountry().getId())
////                .build();
////    }
////
//////    @Transactional(readOnly = true)
//////    public List<WorkspaceInviteResponse> getUserInvites(UUID userId) {
//////        return userWorkspaceRepository.findByUserIdAndStatusFetchWorkspace(userId, MemberStatusEnum.INVITED).stream()
//////                .map(uw -> WorkspaceInviteResponse.builder()
//////                        .id(uw.getId())
//////                        .workspaceName(uw.getWorkspace().getName())
//////                        .invitedBy(uw.getInvitedBy() != null ? uw.getInvitedBy().getFullName() + " (" + uw.getInvitedBy().getEmail() + ")" : "System")
//////                        .status("PENDING")
//////                        .build())
//////                .collect(Collectors.toList());
//////    }
////
////    @Transactional
////    public void acceptInvite(UUID userId, UUID inviteId) {
////        UserWorkspaceEntity uw = userWorkspaceRepository.findById(inviteId)
////                .orElseThrow(() -> new RuntimeException("Invite not found"));
////        if (!uw.getUser().getId().equals(userId)) {
////            throw new RuntimeException("Unauthorized invite access");
////        }
////        uw.setStatus(MemberStatusEnum.ACTIVE);
////        userWorkspaceRepository.save(uw);
////    }
////
////    @Transactional
////    public void declineInvite(UUID userId, UUID inviteId) {
////        UserWorkspaceEntity uw = userWorkspaceRepository.findById(inviteId)
////                .orElseThrow(() -> new RuntimeException("Invite not found"));
////        if (!uw.getUser().getId().equals(userId)) {
////            throw new RuntimeException("Unauthorized invite access");
////        }
////        uw.setStatus(MemberStatusEnum.DEACTIVATED);
////        userWorkspaceRepository.save(uw);
////    }
////
////    @Transactional(readOnly = true)
////    public List<AdminWorkspaceDto> getAllWorkspaces() {
////        List<Workspace> workspaces = workspaceRepository.findAll();
////        List<AdminWorkspaceDto> dtos = new ArrayList<>();
////
////        for (Workspace workspace : workspaces) {
////            List<UserWorkspaceEntity> members = userWorkspaceRepository.findByWorkspaceId(workspace.getId());
////            List<WorkspaceInvoiceEntity> invoices = workspaceInvoiceRepository.findByWorkspaceIdFetchCustomer(workspace.getId());
////
////            String ownerName = "";
////            String ownerEmail = "";
////            for (UserWorkspaceEntity member : members) {
//////                if (member.getRole() == WorkspaceRole.OWNER) {
//////                    if (member.getUser() != null) {
//////                        ownerName = member.getUser().getFullName();
//////                        ownerEmail = member.getUser().getEmail();
//////                        break;
//////                    }
//////                }
////            }
////            if (ownerName.isEmpty() && workspace.getCreatedBy() != null) {
////                ownerName = workspace.getCreatedBy().getFullName();
////                ownerEmail = workspace.getCreatedBy().getEmail();
////            }
////
////            BigDecimal totalBilled = invoices.stream()
////                    .filter(inv -> inv.getStatus() != WorkspaceInvoiceStatus.DRAFT)
////                    .map(WorkspaceInvoiceEntity::getTotalAmount)
////                    .reduce(BigDecimal.ZERO, BigDecimal::add);
////
////            dtos.add(AdminWorkspaceDto.builder()
////                    .id(workspace.getId())
////                    .name(workspace.getName())
////                    .slug(workspace.getSlug())
////                    .ownerName(ownerName)
////                    .ownerEmail(ownerEmail)
////                    .countryId(workspace.getCountry() != null ? workspace.getCountry().getId() : null)
////                    .currencyId(workspace.getCurrency() != null ? workspace.getCurrency().getId() : null)
////                    .features(buildFeatures(workspace))
////                    .suspended(workspace.getStatus() == WorkspaceStatus.SUSPENDED)
////                    .createdAt(workspace.getCreatedAt() != null ? workspace.getCreatedAt().toString() : null)
////                    .memberCount(members.size())
////                    .invoiceCount(invoices.size())
////                    .totalBilled(totalBilled)
////                    .build());
////        }
////
////        return dtos;
////    }
////
////    @Transactional(readOnly = true)
////    public boolean isPlatformAdmin(UUID userId) {
////        return userWorkspaceRepository.findByUserId(userId).stream()
////                .anyMatch(uw -> uw.getWorkspace().getType() == WorkspaceType.PLATFORM_ADMIN);
////    }
}
