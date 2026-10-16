package com.omkarsathe.outvoice.workspace.payment;

import com.omkarsathe.outvoice.common.exception.BadRequestException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.security.CryptoService;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.payment.dto.PaymentGatewayConfigResponse;
import com.omkarsathe.outvoice.workspace.payment.dto.UpdatePaymentGatewayConfigRequest;
import com.omkarsathe.outvoice.workspace.payment.dto.UpdateWorkspacePaymentSettingsRequest;
import com.omkarsathe.outvoice.workspace.payment.dto.WorkspacePaymentSettingsResponse;
import com.omkarsathe.outvoice.workspace.payment.razorpay.RazorpayClientFactory;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspacePaymentSettingsService {

    private final WorkspacePaymentSettingsRepository paymentSettingsRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CryptoService cryptoService;
    private final RazorpayClientFactory razorpayClientFactory;

    @Value("${app.backend-url:http://localhost:8080}")
    private String backendUrl;

    // Backward-compatible method for Razorpay single view
    @Transactional(readOnly = true)
    public WorkspacePaymentSettingsResponse getSettings(UUID workspaceId) {
        return paymentSettingsRepository.findByWorkspaceIdAndProvider(workspaceId, "RAZORPAY")
                .map(this::mapToLegacyResponse)
                .orElse(null);
    }

    // Backward-compatible method for Razorpay update
    @Transactional
    public WorkspacePaymentSettingsResponse updateSettings(UUID workspaceId, UpdateWorkspacePaymentSettingsRequest request) {
        UpdatePaymentGatewayConfigRequest converted = new UpdatePaymentGatewayConfigRequest(
                request.isEnabled(),
                true, // default to primary if legacy method called
                request.keyId(),
                request.keySecret(),
                request.webhookSecret(),
                request.testConnection(),
                null, null, null, null, null, null, null, null, null
        );
        PaymentGatewayConfigResponse res = updateGateway(workspaceId, "RAZORPAY", converted);
        return new WorkspacePaymentSettingsResponse(
                res.id(),
                res.workspaceId(),
                res.provider(),
                res.keyId(),
                res.maskedKeySecret(),
                res.hasWebhookSecret(),
                res.isEnabled(),
                res.webhookUrl(),
                res.updatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<PaymentGatewayConfigResponse> getAllGateways(UUID workspaceId) {
        return paymentSettingsRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToConfigResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PaymentGatewayConfigResponse> getGateway(UUID workspaceId, String provider) {
        return paymentSettingsRepository.findByWorkspaceIdAndProvider(workspaceId, provider.toUpperCase())
                .map(this::mapToConfigResponse);
    }

    @Transactional
    public PaymentGatewayConfigResponse updateGateway(UUID workspaceId, String providerStr, UpdatePaymentGatewayConfigRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        String provider = providerStr.toUpperCase();
        Optional<WorkspacePaymentSettings> existingOpt = paymentSettingsRepository.findByWorkspaceIdAndProvider(workspaceId, provider);

        WorkspacePaymentSettings settings = existingOpt.orElseGet(() ->
                WorkspacePaymentSettings.builder()
                        .workspace(workspace)
                        .provider(provider)
                        .build()
        );

        settings.setEnabled(request.isEnabled());

        // Manage primary status
        List<WorkspacePaymentSettings> allGateways = paymentSettingsRepository.findByWorkspaceId(workspaceId);
        boolean shouldBePrimary = request.isPrimary();

        // If no gateways are primary and this gateway is enabled, automatically designate as primary
        if (!shouldBePrimary && request.isEnabled()) {
            boolean hasOtherPrimary = allGateways.stream()
                    .anyMatch(g -> g.isPrimary() && !g.getProvider().equals(provider) && g.isEnabled());
            if (!hasOtherPrimary) {
                shouldBePrimary = true;
            }
        }

        if (shouldBePrimary) {
            for (WorkspacePaymentSettings other : allGateways) {
                if (!other.getProvider().equals(provider) && other.isPrimary()) {
                    other.setPrimary(false);
                    paymentSettingsRepository.save(other);
                }
            }
            settings.setPrimary(true);
        } else {
            settings.setPrimary(false);
        }

        if ("RAZORPAY".equals(provider)) {
            configureRazorpay(settings, request, existingOpt.isPresent());
        } else if ("UPI".equals(provider)) {
            configureUpi(settings, request);
        } else if ("BANK_TRANSFER".equals(provider)) {
            configureBankTransfer(settings, request);
        } else {
            throw new BadRequestException("Unsupported payment provider: " + provider);
        }

        WorkspacePaymentSettings saved = paymentSettingsRepository.save(settings);
        return mapToConfigResponse(saved);
    }

    @Transactional
    public void setPrimaryGateway(UUID workspaceId, String providerStr) {
        String provider = providerStr.toUpperCase();
        List<WorkspacePaymentSettings> gateways = paymentSettingsRepository.findByWorkspaceId(workspaceId);

        WorkspacePaymentSettings target = gateways.stream()
                .filter(g -> g.getProvider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Payment provider " + provider + " is not configured"));

        if (!target.isEnabled()) {
            throw new BadRequestException("Cannot make a disabled payment gateway primary");
        }

        for (WorkspacePaymentSettings g : gateways) {
            g.setPrimary(g.getProvider().equals(provider));
            paymentSettingsRepository.save(g);
        }
    }

    @Transactional
    public void toggleGateway(UUID workspaceId, String providerStr, boolean isEnabled) {
        String provider = providerStr.toUpperCase();
        WorkspacePaymentSettings settings = paymentSettingsRepository.findByWorkspaceIdAndProvider(workspaceId, provider)
                .orElseThrow(() -> new ResourceNotFoundException("Payment provider " + provider + " is not configured"));

        settings.setEnabled(isEnabled);

        // If disabling primary, reassign primary to another enabled gateway
        if (!isEnabled && settings.isPrimary()) {
            settings.setPrimary(false);
            List<WorkspacePaymentSettings> remaining = paymentSettingsRepository.findByWorkspaceIdAndIsEnabledTrue(workspaceId);
            remaining.stream()
                    .filter(g -> !g.getProvider().equals(provider))
                    .findFirst()
                    .ifPresent(other -> {
                        other.setPrimary(true);
                        paymentSettingsRepository.save(other);
                    });
        }

        paymentSettingsRepository.save(settings);
    }

    @Transactional
    public void disconnectSettings(UUID workspaceId) {
        deleteGateway(workspaceId, "RAZORPAY");
    }

    @Transactional
    public void deleteGateway(UUID workspaceId, String providerStr) {
        String provider = providerStr.toUpperCase();
        paymentSettingsRepository.findByWorkspaceIdAndProvider(workspaceId, provider).ifPresent(settings -> {
            boolean wasPrimary = settings.isPrimary();
            paymentSettingsRepository.delete(settings);
            if ("RAZORPAY".equals(provider)) {
                razorpayClientFactory.evictClient(workspaceId);
            }

            if (wasPrimary) {
                List<WorkspacePaymentSettings> remaining = paymentSettingsRepository.findByWorkspaceIdAndIsEnabledTrue(workspaceId);
                remaining.stream().findFirst().ifPresent(other -> {
                    other.setPrimary(true);
                    paymentSettingsRepository.save(other);
                });
            }
        });
    }

    private void configureRazorpay(WorkspacePaymentSettings settings, UpdatePaymentGatewayConfigRequest request, boolean isExisting) {
        if (request.keyId() == null || request.keyId().isBlank()) {
            throw new BadRequestException("Razorpay Key ID is required");
        }

        String secretToUse;
        if (request.keySecret() != null && !request.keySecret().isBlank()) {
            secretToUse = request.keySecret().trim();
        } else if (isExisting && settings.getKeySecretEncrypted() != null) {
            secretToUse = cryptoService.decrypt(settings.getKeySecretEncrypted());
        } else {
            throw new BadRequestException("Razorpay Key Secret is required for initial setup");
        }

        String keyId = request.keyId().trim();

        if (request.testConnection() || !isExisting) {
            testRazorpayCredentials(keyId, secretToUse);
        }

        settings.setKeyId(keyId);
        settings.setKeySecretEncrypted(cryptoService.encrypt(secretToUse));

        if (request.webhookSecret() != null && !request.webhookSecret().isBlank()) {
            settings.setWebhookSecretEncrypted(cryptoService.encrypt(request.webhookSecret().trim()));
        }

        razorpayClientFactory.evictClient(settings.getWorkspace().getId());
    }

    private void configureUpi(WorkspacePaymentSettings settings, UpdatePaymentGatewayConfigRequest request) {
        if (request.upiId() == null || request.upiId().isBlank()) {
            throw new BadRequestException("UPI ID (VPA) is required (e.g. business@upi)");
        }

        settings.setUpiId(request.upiId().trim());
        settings.setPayeeName(request.payeeName() != null && !request.payeeName().isBlank()
                ? request.payeeName().trim()
                : settings.getWorkspace().getName());
    }

    private void configureBankTransfer(WorkspacePaymentSettings settings, UpdatePaymentGatewayConfigRequest request) {
        if (request.bankAccountNumber() == null || request.bankAccountNumber().isBlank()) {
            throw new BadRequestException("Bank account number is required");
        }
        if (request.bankIfscCode() == null || request.bankIfscCode().isBlank()) {
            throw new BadRequestException("Bank IFSC code is required");
        }

        settings.setBankAccountName(request.bankAccountName() != null && !request.bankAccountName().isBlank()
                ? request.bankAccountName().trim()
                : settings.getWorkspace().getName());
        settings.setBankAccountNumber(request.bankAccountNumber().trim());
        settings.setBankIfscCode(request.bankIfscCode().trim().toUpperCase());
        settings.setBankName(request.bankName() != null ? request.bankName().trim() : null);
        settings.setBankBranch(request.bankBranch() != null ? request.bankBranch().trim() : null);
        settings.setBankSwiftCode(request.bankSwiftCode() != null ? request.bankSwiftCode().trim().toUpperCase() : null);
        settings.setInstructions(request.instructions() != null ? request.instructions().trim() : null);
    }

    private void testRazorpayCredentials(String keyId, String keySecret) {
        try {
            RazorpayClient client = razorpayClientFactory.createDirectClient(keyId, keySecret);
            JSONObject params = new JSONObject();
            params.put("count", 1);
            client.payments.fetchAll(params);
        } catch (Exception e) {
            log.error("Failed to verify Razorpay credentials: {}", e.getMessage());
            throw new BadRequestException("Failed to verify Razorpay credentials: " + e.getMessage());
        }
    }

    private PaymentGatewayConfigResponse mapToConfigResponse(WorkspacePaymentSettings settings) {
        String maskedSecret = null;
        boolean hasWebhook = false;
        String webhookUrl = null;

        if ("RAZORPAY".equals(settings.getProvider())) {
            if (settings.getKeySecretEncrypted() != null) {
                String decryptedSecret = cryptoService.decrypt(settings.getKeySecretEncrypted());
                maskedSecret = maskSecret(decryptedSecret);
            }
            hasWebhook = settings.getWebhookSecretEncrypted() != null && !settings.getWebhookSecretEncrypted().isBlank();
            webhookUrl = backendUrl + "/api/webhooks/razorpay/" + settings.getWorkspace().getId();
        }

        return new PaymentGatewayConfigResponse(
                settings.getId(),
                settings.getWorkspace().getId(),
                settings.getProvider(),
                settings.isEnabled(),
                settings.isPrimary(),

                settings.getKeyId(),
                maskedSecret,
                hasWebhook,
                webhookUrl,

                settings.getUpiId(),
                settings.getPayeeName(),

                settings.getBankAccountName(),
                settings.getBankAccountNumber(),
                settings.getBankIfscCode(),
                settings.getBankName(),
                settings.getBankBranch(),
                settings.getBankSwiftCode(),
                settings.getInstructions(),

                settings.getUpdatedAt()
        );
    }

    private WorkspacePaymentSettingsResponse mapToLegacyResponse(WorkspacePaymentSettings settings) {
        String decryptedSecret = cryptoService.decrypt(settings.getKeySecretEncrypted());
        String maskedSecret = maskSecret(decryptedSecret);
        boolean hasWebhook = settings.getWebhookSecretEncrypted() != null && !settings.getWebhookSecretEncrypted().isBlank();
        String webhookUrl = backendUrl + "/api/webhooks/razorpay/" + settings.getWorkspace().getId();

        return new WorkspacePaymentSettingsResponse(
                settings.getId(),
                settings.getWorkspace().getId(),
                settings.getProvider(),
                settings.getKeyId(),
                maskedSecret,
                hasWebhook,
                settings.isEnabled(),
                webhookUrl,
                settings.getUpdatedAt()
        );
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            return "••••••••";
        }
        if (secret.length() <= 4) {
            return "••••" + secret;
        }
        return "••••••••" + secret.substring(secret.length() - 4);
    }
}
