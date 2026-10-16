package com.omkarsathe.outvoice.workspace.payment;

import com.omkarsathe.outvoice.common.exception.BadRequestException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.security.CryptoService;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
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

    @Transactional(readOnly = true)
    public WorkspacePaymentSettingsResponse getSettings(UUID workspaceId) {
        return paymentSettingsRepository.findByWorkspaceId(workspaceId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional
    public WorkspacePaymentSettingsResponse updateSettings(UUID workspaceId, UpdateWorkspacePaymentSettingsRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        Optional<WorkspacePaymentSettings> existingOpt = paymentSettingsRepository.findByWorkspaceId(workspaceId);

        String secretToUse;
        if (request.keySecret() != null && !request.keySecret().isBlank()) {
            secretToUse = request.keySecret().trim();
        } else if (existingOpt.isPresent()) {
            secretToUse = cryptoService.decrypt(existingOpt.get().getKeySecretEncrypted());
        } else {
            throw new BadRequestException("Razorpay Key Secret is required for initial setup");
        }

        String keyId = request.keyId().trim();

        // Validate credentials against Razorpay API if testConnection is true or for initial connection
        if (request.testConnection() || existingOpt.isEmpty()) {
            testRazorpayCredentials(keyId, secretToUse);
        }

        WorkspacePaymentSettings settings = existingOpt.orElseGet(() ->
                WorkspacePaymentSettings.builder()
                        .workspace(workspace)
                        .provider("RAZORPAY")
                        .build()
        );

        settings.setKeyId(keyId);
        settings.setKeySecretEncrypted(cryptoService.encrypt(secretToUse));
        settings.setEnabled(request.isEnabled());

        if (request.webhookSecret() != null && !request.webhookSecret().isBlank()) {
            settings.setWebhookSecretEncrypted(cryptoService.encrypt(request.webhookSecret().trim()));
        }

        WorkspacePaymentSettings saved = paymentSettingsRepository.save(settings);
        razorpayClientFactory.evictClient(workspaceId);

        return mapToResponse(saved);
    }

    @Transactional
    public void disconnectSettings(UUID workspaceId) {
        paymentSettingsRepository.findByWorkspaceId(workspaceId).ifPresent(settings -> {
            paymentSettingsRepository.delete(settings);
            razorpayClientFactory.evictClient(workspaceId);
        });
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

    private WorkspacePaymentSettingsResponse mapToResponse(WorkspacePaymentSettings settings) {
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
