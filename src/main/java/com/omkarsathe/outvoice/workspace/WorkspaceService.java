package com.omkarsathe.outvoice.workspace;

import com.omkarsathe.outvoice.country.Country;
import com.omkarsathe.outvoice.country.CountryRepository;
import com.omkarsathe.outvoice.currency.Currency;
import com.omkarsathe.outvoice.currency.CurrencyRepository;
import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.user.UserRepository;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
import com.omkarsathe.outvoice.workspace.dto.WorkspaceRequest;
import com.omkarsathe.outvoice.workspace.dto.WorkspaceResponse;
import com.omkarsathe.outvoice.workspace.member.MemberStatusEnum;
import com.omkarsathe.outvoice.workspace.role.WorkspaceRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final UserRepository userRepository;
    private final UserWorkspaceRepository userWorkspaceRepository;
    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getUserWorkspaces(UUID userId) {
        return userWorkspaceRepository.findByUserIdAndStatusFetchWorkspace(userId, MemberStatusEnum.ACTIVE).stream()
                .map(uw -> WorkspaceResponse.builder()
                        .id(uw.getWorkspace().getId())
                        .name(uw.getWorkspace().getName())
                        .slug(uw.getWorkspace().getSlug())
                        .role(uw.getRole())
                        .isDefault(uw.getIsDefaultWorkspace() != null ? uw.getIsDefaultWorkspace() : false)
                        .currencyId(uw.getWorkspace().getCurrency() != null ? uw.getWorkspace().getCurrency().getId() : null)
                        .countryId(uw.getWorkspace().getCountry() != null ? uw.getWorkspace().getCountry().getId() : null)
                        .build())
                .collect(Collectors.toList());
    }

    public WorkspaceResponse createUserWorkspace(UUID userId, WorkspaceRequest request) {
        UserEntity createdBy = userRepository.getUserById(userId)
                .orElseThrow(
                        () -> new RuntimeException("User with id " + userId + " does not exist")
                );

        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(
                        () -> new RuntimeException("Country with id " + request.getCountryId() + " does not exist")
                );

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(
                        () -> new RuntimeException("Country with id " + request.getCountryId() + " does not exist")
                );

        WorkspaceEntity workspace = WorkspaceEntity.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .country(country)
                .currency(currency)
                .taxComplianceName(request.getName())
                .status(WorkspaceStatus.ACTIVE)
                .isPlaceholder(false)
                .createdBy(createdBy)
                .build();

        workspace = workspaceRepository.save(workspace);

        UserWorkspaceEntity userWorkspace = UserWorkspaceEntity.builder()
                .user(createdBy)
                .workspace(workspace)
                .role(WorkspaceRole.OWNER)
                .isDefaultWorkspace(false)
                .invitedBy(null)
                .joinedAt(null)
                .status(MemberStatusEnum.ACTIVE)
                .build();

        userWorkspace = userWorkspaceRepository.save(userWorkspace);

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .slug(workspace.getSlug())
                .role(userWorkspace.getRole())
                .isDefault(userWorkspace.getIsDefaultWorkspace() != null ? userWorkspace.getIsDefaultWorkspace() : false)
                .currencyId(workspace.getCurrency().getId())
                .countryId(workspace.getCountry().getId())
                .build();
    }

//    @Transactional(readOnly = true)
//    public List<WorkspaceInviteResponse> getUserInvites(UUID userId) {
//        return userWorkspaceRepository.findByUserIdAndStatusFetchWorkspace(userId, MemberStatusEnum.INVITED).stream()
//                .map(uw -> WorkspaceInviteResponse.builder()
//                        .id(uw.getId())
//                        .workspaceName(uw.getWorkspace().getName())
//                        .invitedBy(uw.getInvitedBy() != null ? uw.getInvitedBy().getFullName() + " (" + uw.getInvitedBy().getEmail() + ")" : "System")
//                        .status("PENDING")
//                        .build())
//                .collect(Collectors.toList());
//    }

    @Transactional
    public void acceptInvite(UUID userId, UUID inviteId) {
        UserWorkspaceEntity uw = userWorkspaceRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        if (!uw.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized invite access");
        }
        uw.setStatus(MemberStatusEnum.ACTIVE);
        userWorkspaceRepository.save(uw);
    }

    @Transactional
    public void declineInvite(UUID userId, UUID inviteId) {
        UserWorkspaceEntity uw = userWorkspaceRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        if (!uw.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized invite access");
        }
        uw.setStatus(MemberStatusEnum.DEACTIVATED);
        userWorkspaceRepository.save(uw);
    }
}
