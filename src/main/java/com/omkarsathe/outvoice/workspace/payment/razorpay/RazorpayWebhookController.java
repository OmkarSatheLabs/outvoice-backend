package com.omkarsathe.outvoice.workspace.payment.razorpay;

import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceStatus;
import com.omkarsathe.outvoice.workspace.payment.Payment;
import com.omkarsathe.outvoice.workspace.payment.PaymentRepository;
import com.omkarsathe.outvoice.workspace.payment.PaymentStatus;
import com.omkarsathe.outvoice.workspace.payment.WorkspacePaymentSettings;
import com.omkarsathe.outvoice.workspace.payment.WorkspacePaymentSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/webhooks/razorpay")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final WorkspacePaymentSettingsRepository paymentSettingsRepository;
    private final RazorpayClientFactory razorpayClientFactory;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @PostMapping("/{workspaceId}")
    @Transactional
    public ResponseEntity<Map<String, String>> handleWebhook(
            @PathVariable UUID workspaceId,
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signatureHeader
    ) {
        log.info("Received Razorpay webhook for workspace: {}", workspaceId);

        Optional<WorkspacePaymentSettings> settingsOpt = paymentSettingsRepository.findByWorkspaceId(workspaceId);
        if (settingsOpt.isEmpty() || !settingsOpt.get().isEnabled()) {
            log.warn("Webhook ignored: Razorpay settings not found or disabled for workspace {}", workspaceId);
            return ResponseEntity.ok(Map.of("status", "ignored"));
        }

        WorkspacePaymentSettings settings = settingsOpt.get();
        String webhookSecret = razorpayClientFactory.getDecryptedWebhookSecret(settings);

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook secret not configured for workspace {}. Falling back to key secret.", workspaceId);
            webhookSecret = razorpayClientFactory.getDecryptedKeySecret(settings);
        }

        if (signatureHeader == null || !verifyWebhookSignature(rawPayload, signatureHeader, webhookSecret)) {
            log.error("Invalid webhook signature for workspace {}", workspaceId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid signature"));
        }

        try {
            JSONObject eventJson = new JSONObject(rawPayload);
            String event = eventJson.optString("event");
            log.info("Processing Razorpay event: {} for workspace: {}", event, workspaceId);

            JSONObject payloadObj = eventJson.optJSONObject("payload");
            if (payloadObj == null) {
                return ResponseEntity.ok(Map.of("status", "processed"));
            }

            JSONObject paymentEntity = null;
            if (payloadObj.has("payment")) {
                paymentEntity = payloadObj.getJSONObject("payment").optJSONObject("entity");
            }

            if (paymentEntity != null) {
                processPaymentEntity(event, paymentEntity, workspaceId);
            }

            return ResponseEntity.ok(Map.of("status", "processed"));
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook for workspace {}: {}", workspaceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    private void processPaymentEntity(String event, JSONObject paymentEntity, UUID workspaceId) {
        String paymentId = paymentEntity.optString("id");
        String orderId = paymentEntity.optString("order_id");
        long amountPaise = paymentEntity.optLong("amount", 0);
        BigDecimal amount = BigDecimal.valueOf(amountPaise).divide(BigDecimal.valueOf(100));
        String method = paymentEntity.optString("method");

        JSONObject notes = paymentEntity.optJSONObject("notes");
        String invoiceIdStr = notes != null ? notes.optString("invoice_id") : null;

        Optional<Payment> paymentOpt = Optional.empty();
        if (orderId != null && !orderId.isBlank()) {
            paymentOpt = paymentRepository.findByOrderId(orderId);
        }
        if (paymentOpt.isEmpty() && paymentId != null && !paymentId.isBlank()) {
            paymentOpt = paymentRepository.findByProviderPaymentId(paymentId);
        }

        if ("payment.captured".equals(event) || "order.paid".equals(event)) {
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setProviderPaymentId(paymentId);
                payment.setPaymentMethod(method);
                payment.setCompletedAt(Instant.now());
                paymentRepository.save(payment);

                Invoice invoice = payment.getInvoice();
                if (invoice != null && invoice.getStatus() != InvoiceStatus.PAID) {
                    invoice.setStatus(InvoiceStatus.PAID);
                    invoiceRepository.save(invoice);
                    log.info("Invoice {} updated to PAID via webhook event {}", invoice.getId(), event);
                }
            } else if (invoiceIdStr != null && !invoiceIdStr.isBlank()) {
                try {
                    UUID invoiceId = UUID.fromString(invoiceIdStr);
                    Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
                    if (invoiceOpt.isPresent()) {
                        Invoice invoice = invoiceOpt.get();
                        Payment payment = Payment.builder()
                                .invoice(invoice)
                                .workspace(invoice.getWorkspace())
                                .amount(amount)
                                .status(PaymentStatus.SUCCESS)
                                .provider("RAZORPAY")
                                .orderId(orderId)
                                .providerPaymentId(paymentId)
                                .paymentMethod(method)
                                .completedAt(Instant.now())
                                .build();
                        paymentRepository.save(payment);

                        invoice.setStatus(InvoiceStatus.PAID);
                        invoiceRepository.save(invoice);
                        log.info("Created new successful payment and marked invoice {} as PAID", invoice.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to parse invoice ID from notes: {}", invoiceIdStr, e);
                }
            }
        } else if ("payment.failed".equals(event)) {
            paymentOpt.ifPresent(p -> {
                p.setStatus(PaymentStatus.FAILED);
                JSONObject error = paymentEntity.optJSONObject("error_description");
                if (error != null) {
                    p.setErrorDescription(error.toString());
                }
                paymentRepository.save(p);
            });
        }
    }

    private boolean verifyWebhookSignature(String payload, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hash);

            return MessageDigest.isEqual(
                    generatedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Webhook signature verification error: {}", e.getMessage());
            return false;
        }
    }
}
