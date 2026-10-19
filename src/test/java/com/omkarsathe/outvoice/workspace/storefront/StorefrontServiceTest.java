package com.omkarsathe.outvoice.workspace.storefront;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorefrontServiceTest {

    @Mock
    private StorefrontRepository storefrontRepository;
    @Mock
    private StorefrontDomainRepository storefrontDomainRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private StorefrontResponseMapper storefrontResponseMapper;
    @Mock
    private DnsVerificationService dnsVerificationService;
    @Mock
    private TlsCertificateService tlsCertificateService;
    @Mock
    private AuditService auditService;
    @Mock
    private StorefrontUrlService storefrontUrlService;

    private StorefrontService storefrontService;

    private final UUID workspaceId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private Workspace workspace;
    private Storefront storefront;

    @BeforeEach
    void setUp() {
        storefrontService = new StorefrontService(
                storefrontRepository,
                storefrontDomainRepository,
                workspaceRepository,
                memberRepository,
                storefrontResponseMapper,
                dnsVerificationService,
                tlsCertificateService,
                auditService,
                storefrontUrlService
        );

        workspace = Workspace.builder()
                .id(workspaceId)
                .name("Acme Studio")
                .build();

        storefront = Storefront.builder()
                .id(UUID.randomUUID())
                .workspace(workspace)
                .slug("acme-studio")
                .name("Acme Studio Storefront")
                .description("Public store")
                .urlStrategy(UrlStrategy.PATH_BASED)
                .status(StorefrontStatus.DRAFT)
                .brandingConfig("{}")
                .pagesConfig("{}")
                .domains(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getOrCreateStorefront_returnsExisting_whenPresent() {
        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(storefrontRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(storefront));

        StorefrontResponse mockResponse = new StorefrontResponse(
                storefront.getId(), workspaceId, "acme-studio", "Acme Studio Storefront",
                "Public store", null, UrlStrategy.PATH_BASED, StorefrontStatus.DRAFT,
                "{}", "{}", "https://dev.outvoice.omkarsathe.com/acme-studio",
                Collections.emptyList(), Instant.now(), Instant.now()
        );
        when(storefrontResponseMapper.toResponse(storefront)).thenReturn(mockResponse);

        StorefrontResponse response = storefrontService.getOrCreateStorefront(workspaceId, userId);

        assertNotNull(response);
        assertEquals("acme-studio", response.slug());
        verify(storefrontRepository, never()).save(any());
    }

    @Test
    void getOrCreateStorefront_createsDefault_whenNotPresent() {
        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(storefrontRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.empty());
        when(storefrontRepository.existsBySlugIgnoreCase("acme-studio")).thenReturn(false);
        when(storefrontRepository.save(any(Storefront.class))).thenAnswer(inv -> inv.getArgument(0));

        StorefrontResponse mockResponse = new StorefrontResponse(
                UUID.randomUUID(), workspaceId, "acme-studio", "Acme Studio",
                null, null, UrlStrategy.PATH_BASED, StorefrontStatus.DRAFT,
                "{}", "{}", "https://dev.outvoice.omkarsathe.com/acme-studio",
                Collections.emptyList(), Instant.now(), Instant.now()
        );
        when(storefrontResponseMapper.toResponse(any())).thenReturn(mockResponse);

        StorefrontResponse response = storefrontService.getOrCreateStorefront(workspaceId, userId);

        assertNotNull(response);
        verify(storefrontRepository).save(any(Storefront.class));
    }

    @Test
    void getOrCreateStorefront_throwsForbidden_whenUserNotMember() {
        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () ->
                storefrontService.getOrCreateStorefront(workspaceId, userId));
    }

    @Test
    void updateStorefront_updatesFields_successfully() {
        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(storefrontRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(storefront));
        when(storefrontRepository.existsBySlugIgnoreCaseAndWorkspaceIdNot("acme-new-slug", workspaceId)).thenReturn(false);
        when(storefrontRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateStorefrontRequest req = new UpdateStorefrontRequest(
                "acme-new-slug", "Acme Updated", "New description", null,
                UrlStrategy.OUTVOICE_SUBDOMAIN, "{\"primaryColor\":\"#ff0000\"}", "{}"
        );

        StorefrontResponse mockResponse = new StorefrontResponse(
                storefront.getId(), workspaceId, "acme-new-slug", "Acme Updated",
                "New description", null, UrlStrategy.OUTVOICE_SUBDOMAIN, StorefrontStatus.DRAFT,
                "{\"primaryColor\":\"#ff0000\"}", "{}", "https://acme-new-slug.dev.outvoice.omkarsathe.com",
                Collections.emptyList(), Instant.now(), Instant.now()
        );
        when(storefrontResponseMapper.toResponse(any())).thenReturn(mockResponse);

        StorefrontResponse res = storefrontService.updateStorefront(workspaceId, req, userId);

        assertNotNull(res);
        assertEquals("acme-new-slug", res.slug());
        assertEquals("Acme Updated", res.name());
        verify(auditService).log(eq(workspaceId), any(), any(), any(), anyString(), anyMap());
    }

    @Test
    void updateStorefront_throwsBadRequest_whenSlugIsReserved() {
        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(storefrontRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(storefront));

        UpdateStorefrontRequest req = new UpdateStorefrontRequest(
                "admin", "Acme", "desc", null, UrlStrategy.PATH_BASED, null, null
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                storefrontService.updateStorefront(workspaceId, req, userId));
        assertTrue(ex.getMessage().contains("reserved"));
    }

    @Test
    void updateStorefront_throwsConflict_whenSlugAlreadyTaken() {
        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(storefrontRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(storefront));
        when(storefrontRepository.existsBySlugIgnoreCaseAndWorkspaceIdNot("taken-slug", workspaceId)).thenReturn(true);

        UpdateStorefrontRequest req = new UpdateStorefrontRequest(
                "taken-slug", "Acme", "desc", null, UrlStrategy.PATH_BASED, null, null
        );

        ConflictException ex = assertThrows(ConflictException.class, () ->
                storefrontService.updateStorefront(workspaceId, req, userId));
        assertTrue(ex.getMessage().contains("already in use"));
    }

    @Test
    void publishStorefront_changesStatusToPublished() {
        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(storefrontRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(storefront));
        when(storefrontRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storefrontService.publishStorefront(workspaceId, userId);

        assertEquals(StorefrontStatus.PUBLISHED, storefront.getStatus());
        verify(auditService).log(eq(workspaceId), any(), any(), any(), contains("Published"), anyMap());
    }

    @Test
    void unpublishStorefront_changesStatusToDraft() {
        storefront.setStatus(StorefrontStatus.PUBLISHED);
        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(storefrontRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(storefront));
        when(storefrontRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storefrontService.unpublishStorefront(workspaceId, userId);

        assertEquals(StorefrontStatus.DRAFT, storefront.getStatus());
        verify(auditService).log(eq(workspaceId), any(), any(), any(), contains("Unpublished"), anyMap());
    }

    @Test
    void addCustomDomain_successfullyAddsPendingDomain() {
        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(storefrontRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(storefront));
        when(storefrontDomainRepository.existsByHostnameIgnoreCase("store.acme.com")).thenReturn(false);
        when(dnsVerificationService.generateVerificationToken()).thenReturn("ov_verify_token_123");
        when(dnsVerificationService.hashToken("ov_verify_token_123")).thenReturn("hash_123");
        when(storefrontDomainRepository.save(any(StorefrontDomain.class))).thenAnswer(inv -> {
            StorefrontDomain d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        StorefrontDomainResponse mockRes = new StorefrontDomainResponse(
                UUID.randomUUID(), "store.acme.com", DomainVerificationStatus.PENDING,
                "ov_verify_token_123", "TXT", "_outvoice-challenge.store.acme.com", "ov_verify_token_123",
                null, TlsStatus.PENDING, true, Instant.now()
        );
        when(storefrontResponseMapper.toDomainResponse(any())).thenReturn(mockRes);

        StorefrontDomainResponse res = storefrontService.addCustomDomain(
                workspaceId, new AddStorefrontDomainRequest("store.acme.com"), userId
        );

        assertNotNull(res);
        assertEquals("store.acme.com", res.hostname());
        assertEquals(DomainVerificationStatus.PENDING, res.verificationStatus());
    }

    @Test
    void verifyCustomDomain_marksVerified_whenDnsMatches() {
        UUID domainId = UUID.randomUUID();
        StorefrontDomain domain = StorefrontDomain.builder()
                .id(domainId)
                .storefront(storefront)
                .hostname("store.acme.com")
                .verificationToken("ov_verify_123")
                .verificationStatus(DomainVerificationStatus.PENDING)
                .tlsStatus(TlsStatus.PENDING)
                .build();

        when(memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(true);
        when(storefrontRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(storefront));
        when(storefrontDomainRepository.findByIdAndStorefrontId(domainId, storefront.getId())).thenReturn(Optional.of(domain));
        when(dnsVerificationService.verifyChallenge("store.acme.com", "ov_verify_123")).thenReturn(true);
        when(tlsCertificateService.provisionOrCheckCertificate(domain)).thenReturn(TlsStatus.ACTIVE);
        when(storefrontDomainRepository.save(domain)).thenReturn(domain);

        storefrontService.verifyCustomDomain(workspaceId, domainId, userId);

        assertEquals(DomainVerificationStatus.VERIFIED, domain.getVerificationStatus());
        assertNotNull(domain.getVerifiedAt());
        assertEquals(TlsStatus.ACTIVE, domain.getTlsStatus());
    }
}
