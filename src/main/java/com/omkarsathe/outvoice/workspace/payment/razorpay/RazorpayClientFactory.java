package com.omkarsathe.outvoice.workspace.payment.razorpay;

import com.omkarsathe.outvoice.common.exception.PaymentGatewayException;
import com.omkarsathe.outvoice.security.CryptoService;
import com.omkarsathe.outvoice.workspace.payment.WorkspacePaymentSettings;
import com.omkarsathe.outvoice.workspace.payment.WorkspacePaymentSettingsRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RazorpayClientFactory {

    private final WorkspacePaymentSettingsRepository paymentSettingsRepository;
    private final CryptoService cryptoService;

    // Cache RazorpayClient instances by workspaceId
    private final Map<UUID, RazorpayClient> clientCache = new ConcurrentHashMap<>();

    public RazorpayClient getClient(UUID workspaceId) {
        return clientCache.computeIfAbsent(workspaceId, this::buildClientForWorkspace);
    }

    public RazorpayClient createDirectClient(String keyId, String keySecret) {
        try {
            return new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            throw new PaymentGatewayException("Failed to initialize RazorpayClient: " + e.getMessage(), e);
        }
    }

    public void evictClient(UUID workspaceId) {
        clientCache.remove(workspaceId);
    }

    public Optional<WorkspacePaymentSettings> getSettings(UUID workspaceId) {
        return paymentSettingsRepository.findByWorkspaceIdAndProvider(workspaceId, "RAZORPAY");
    }

    public String getDecryptedKeySecret(WorkspacePaymentSettings settings) {
        return cryptoService.decrypt(settings.getKeySecretEncrypted());
    }

    public String getDecryptedWebhookSecret(WorkspacePaymentSettings settings) {
        if (settings.getWebhookSecretEncrypted() == null || settings.getWebhookSecretEncrypted().isBlank()) {
            return null;
        }
        return cryptoService.decrypt(settings.getWebhookSecretEncrypted());
    }

    private RazorpayClient buildClientForWorkspace(UUID workspaceId) {
        WorkspacePaymentSettings settings = paymentSettingsRepository.findByWorkspaceIdAndProvider(workspaceId, "RAZORPAY")
                .orElseThrow(() -> new PaymentGatewayException("Razorpay gateway is not configured for workspace " + workspaceId));

        if (!settings.isEnabled()) {
            throw new PaymentGatewayException("Razorpay gateway is disabled for workspace " + workspaceId);
        }

        String keyId = settings.getKeyId();
        String keySecret = cryptoService.decrypt(settings.getKeySecretEncrypted());

        return createDirectClient(keyId, keySecret);
    }
}
