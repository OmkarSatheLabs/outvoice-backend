package com.omkarsathe.outvoice.workspace.storefront;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
import com.omkarsathe.outvoice.common.exception.BadRequestException;
import com.omkarsathe.outvoice.common.exception.ConflictException;
import com.omkarsathe.outvoice.common.exception.ForbiddenException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.member.MemberRepository;
import com.omkarsathe.outvoice.workspace.storefront.dto.AddStorefrontDomainRequest;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontDomainResponse;
import com.omkarsathe.outvoice.workspace.storefront.dto.StorefrontResponse;
import com.omkarsathe.outvoice.workspace.storefront.dto.UpdateStorefrontRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorefrontService {

    private final StorefrontRepository storefrontRepository;
    private final StorefrontDomainRepository storefrontDomainRepository;
    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;
    private final StorefrontResponseMapper storefrontResponseMapper;
    private final DnsVerificationService dnsVerificationService;
    private final TlsCertificateService tlsCertificateService;
    private final AuditService auditService;
    private final StorefrontUrlService storefrontUrlService;

    @Transactional
    public StorefrontResponse getOrCreateStorefront(UUID workspaceId, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        Storefront storefront = storefrontRepository.findByWorkspaceId(workspaceId)
                .orElseGet(() -> createDefaultStorefront(workspace));

        return storefrontResponseMapper.toResponse(storefront);
    }

    @Transactional
    public StorefrontResponse updateStorefront(UUID workspaceId, UpdateStorefrontRequest request, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Storefront storefront = storefrontRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found for workspace: " + workspaceId));

        String normalizedSlug = ReservedSlugs.normalizeSlug(request.slug());
        ReservedSlugs.validateSlug(normalizedSlug);

        if (storefrontRepository.existsBySlugIgnoreCaseAndWorkspaceIdNot(normalizedSlug, workspaceId)) {
            throw new ConflictException("Slug '" + normalizedSlug + "' is already in use by another workspace");
        }

        storefront.setSlug(normalizedSlug);
        storefront.setName(request.name().trim());
        storefront.setDescription(request.description() != null ? request.description().trim() : null);
        storefront.setLogoUrl(request.logoUrl() != null && !request.logoUrl().isBlank() ? request.logoUrl().trim() : null);

        if (request.urlStrategy() != null) {
            storefront.setUrlStrategy(request.urlStrategy());
        }
        if (request.brandingConfig() != null && !request.brandingConfig().isBlank()) {
            storefront.setBrandingConfig(request.brandingConfig().trim());
        }
        if (request.pagesConfig() != null && !request.pagesConfig().isBlank()) {
            storefront.setPagesConfig(request.pagesConfig().trim());
        }

        Storefront saved = storefrontRepository.save(storefront);

        auditService.log(
                workspaceId,
                AuditAction.UPDATE,
                AuditResourceType.STOREFRONT,
                saved.getId(),
                "Updated storefront settings for " + saved.getName(),
                Map.of("slug", saved.getSlug(), "strategy", saved.getUrlStrategy().name(), "status", saved.getStatus().name())
        );

        return storefrontResponseMapper.toResponse(saved);
    }

    @Transactional
    public StorefrontResponse publishStorefront(UUID workspaceId, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Storefront storefront = storefrontRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found for workspace: " + workspaceId));

        storefront.setStatus(StorefrontStatus.PUBLISHED);
        Storefront saved = storefrontRepository.save(storefront);

        auditService.log(
                workspaceId,
                AuditAction.UPDATE,
                AuditResourceType.STOREFRONT,
                saved.getId(),
                "Published storefront " + saved.getName(),
                Map.of("status", saved.getStatus().name(), "slug", saved.getSlug())
        );

        return storefrontResponseMapper.toResponse(saved);
    }

    @Transactional
    public StorefrontResponse unpublishStorefront(UUID workspaceId, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Storefront storefront = storefrontRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found for workspace: " + workspaceId));

        storefront.setStatus(StorefrontStatus.DRAFT);
        Storefront saved = storefrontRepository.save(storefront);

        auditService.log(
                workspaceId,
                AuditAction.UPDATE,
                AuditResourceType.STOREFRONT,
                saved.getId(),
                "Unpublished storefront " + saved.getName(),
                Map.of("status", saved.getStatus().name(), "slug", saved.getSlug())
        );

        return storefrontResponseMapper.toResponse(saved);
    }

    @Transactional
    public StorefrontResponse toggleStorefront(UUID workspaceId, boolean enable, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Storefront storefront = storefrontRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found for workspace: " + workspaceId));

        if (enable) {
            storefront.setStatus(StorefrontStatus.PUBLISHED);
        } else {
            storefront.setStatus(StorefrontStatus.DISABLED);
        }
        Storefront saved = storefrontRepository.save(storefront);

        auditService.log(
                workspaceId,
                AuditAction.UPDATE,
                AuditResourceType.STOREFRONT,
                saved.getId(),
                (enable ? "Enabled" : "Disabled") + " storefront " + saved.getName(),
                Map.of("status", saved.getStatus().name())
        );

        return storefrontResponseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StorefrontDomainResponse> getDomains(UUID workspaceId, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Storefront storefront = storefrontRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found for workspace: " + workspaceId));

        return storefrontDomainRepository.findAllByStorefrontId(storefront.getId())
                .stream()
                .map(storefrontResponseMapper::toDomainResponse)
                .toList();
    }

    @Transactional
    public StorefrontDomainResponse addCustomDomain(UUID workspaceId, AddStorefrontDomainRequest request, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Storefront storefront = storefrontRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found for workspace: " + workspaceId));

        String normalizedHostname = ReservedSlugs.normalizeHostname(request.hostname());
        if (normalizedHostname.isBlank() || !normalizedHostname.contains(".")) {
            throw new BadRequestException("Invalid hostname format: " + request.hostname());
        }

        // Prevent claiming localhost or platform base domain as a custom domain
        if (normalizedHostname.equals("localhost") || normalizedHostname.endsWith(".localhost")) {
            throw new BadRequestException("Cannot register localhost as custom domain");
        }

        if (storefrontDomainRepository.existsByHostnameIgnoreCase(normalizedHostname)) {
            throw new ConflictException("Domain '" + normalizedHostname + "' is already registered to a storefront");
        }

        String verificationToken = dnsVerificationService.generateVerificationToken();
        String tokenHash = dnsVerificationService.hashToken(verificationToken);

        List<StorefrontDomain> existingDomains = storefrontDomainRepository.findAllByStorefrontId(storefront.getId());
        boolean isFirst = existingDomains.isEmpty();

        StorefrontDomain domain = StorefrontDomain.builder()
                .storefront(storefront)
                .hostname(normalizedHostname)
                .verificationStatus(DomainVerificationStatus.PENDING)
                .verificationToken(verificationToken)
                .verificationTokenHash(tokenHash)
                .tlsStatus(TlsStatus.PENDING)
                .isPrimary(isFirst)
                .build();

        StorefrontDomain saved = storefrontDomainRepository.save(domain);

        auditService.log(
                workspaceId,
                AuditAction.CREATE,
                AuditResourceType.STOREFRONT,
                saved.getId(),
                "Added custom domain " + saved.getHostname(),
                Map.of("hostname", saved.getHostname())
        );

        return storefrontResponseMapper.toDomainResponse(saved);
    }

    @Transactional
    public StorefrontDomainResponse verifyCustomDomain(UUID workspaceId, UUID domainId, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Storefront storefront = storefrontRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found for workspace: " + workspaceId));

        StorefrontDomain domain = storefrontDomainRepository.findByIdAndStorefrontId(domainId, storefront.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + domainId));

        boolean verified = dnsVerificationService.verifyChallenge(domain.getHostname(), domain.getVerificationToken());
        if (verified) {
            domain.setVerificationStatus(DomainVerificationStatus.VERIFIED);
            domain.setVerifiedAt(Instant.now());
            domain.setTlsStatus(tlsCertificateService.provisionOrCheckCertificate(domain));
        } else {
            domain.setVerificationStatus(DomainVerificationStatus.FAILED);
        }

        StorefrontDomain saved = storefrontDomainRepository.save(domain);

        auditService.log(
                workspaceId,
                AuditAction.UPDATE,
                AuditResourceType.STOREFRONT,
                saved.getId(),
                "Attempted DNS verification for domain " + saved.getHostname() + ": " + saved.getVerificationStatus(),
                Map.of("hostname", saved.getHostname(), "status", saved.getVerificationStatus().name())
        );

        return storefrontResponseMapper.toDomainResponse(saved);
    }

    @Transactional
    public void deleteCustomDomain(UUID workspaceId, UUID domainId, UUID currentUserId) {
        validateWorkspaceAccess(workspaceId, currentUserId);

        Storefront storefront = storefrontRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found for workspace: " + workspaceId));

        StorefrontDomain domain = storefrontDomainRepository.findByIdAndStorefrontId(domainId, storefront.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + domainId));

        storefrontDomainRepository.delete(domain);

        auditService.log(
                workspaceId,
                AuditAction.DELETE,
                AuditResourceType.STOREFRONT,
                domain.getId(),
                "Deleted custom domain " + domain.getHostname(),
                Map.of("hostname", domain.getHostname())
        );
    }

    private Storefront createDefaultStorefront(Workspace workspace) {
        String baseSlug = workspace.getName() != null
                ? workspace.getName().toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "")
                : "workspace";
        if (baseSlug.length() < 3) {
            baseSlug = "workspace-" + baseSlug;
        }
        if (ReservedSlugs.isReserved(baseSlug)) {
            baseSlug = baseSlug + "-store";
        }

        String uniqueSlug = baseSlug;
        int counter = 1;
        while (storefrontRepository.existsBySlugIgnoreCase(uniqueSlug) || ReservedSlugs.isReserved(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter++;
        }

        Storefront defaultStorefront = Storefront.builder()
                .workspace(workspace)
                .name(workspace.getName() != null ? workspace.getName() : "My Store")
                .slug(uniqueSlug)
                .urlStrategy(UrlStrategy.PATH_BASED)
                .status(StorefrontStatus.DRAFT)
                .brandingConfig("{\"primaryColor\":\"#4f46e5\",\"secondaryColor\":\"#059669\",\"font\":\"Inter\"}")
                .pagesConfig("{\"defaultPages\":{\"home\":true,\"products\":true,\"auth\":true,\"cart\":true}}")
                .build();

        return storefrontRepository.save(defaultStorefront);
    }

    private void validateWorkspaceAccess(UUID workspaceId, UUID currentUserId) {
        if (currentUserId != null) {
            boolean isMember = memberRepository.existsByWorkspaceIdAndUserId(workspaceId, currentUserId);
            if (!isMember) {
                throw new ForbiddenException("You are not a member of this workspace", workspaceId.toString());
            }
        }
    }
}
