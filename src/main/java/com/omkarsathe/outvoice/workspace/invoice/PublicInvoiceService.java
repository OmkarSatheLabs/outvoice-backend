package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.common.exception.BadRequestException;
import com.omkarsathe.outvoice.common.exception.PaymentGatewayException;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.customer.Customer;
import com.omkarsathe.outvoice.workspace.invoice.dto.*;
import com.omkarsathe.outvoice.workspace.payment.Payment;
import com.omkarsathe.outvoice.workspace.payment.PaymentRepository;
import com.omkarsathe.outvoice.workspace.payment.PaymentStatus;
import com.omkarsathe.outvoice.workspace.payment.WorkspacePaymentSettings;
import com.omkarsathe.outvoice.workspace.payment.WorkspacePaymentSettingsRepository;
import com.omkarsathe.outvoice.workspace.payment.razorpay.RazorpayClientFactory;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicInvoiceService {

    private final InvoiceAccessTokenService tokenService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final WorkspacePaymentSettingsRepository paymentSettingsRepository;
    private final RazorpayClientFactory razorpayClientFactory;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PublicInvoiceDto getPublicInvoice(String token) {
        Invoice invoice = tokenService.resolveInvoiceByToken(token);

        // Mark as VIEWED if invoice is currently in SENT status
        if (invoice.getStatus() == InvoiceStatus.SENT) {
            invoice.setStatus(InvoiceStatus.VIEWED);
            invoice = invoiceRepository.save(invoice);
        }

        final Invoice currentInvoice = invoice;
        Workspace workspace = invoice.getWorkspace();
        Customer customer = invoice.getCustomer();

        List<WorkspacePaymentSettings> enabledSettings = paymentSettingsRepository
                .findByWorkspaceIdAndIsEnabledTrue(workspace.getId());
        boolean paymentEnabled = !enabledSettings.isEmpty();

        // Determine primary payment method
        WorkspacePaymentSettings primary = enabledSettings.stream()
                .filter(WorkspacePaymentSettings::isPrimary)
                .findFirst()
                .orElse(enabledSettings.isEmpty() ? null : enabledSettings.get(0));

        String primaryPaymentMethod = primary != null ? primary.getProvider() : null;

        List<PaymentMethodDto> paymentMethods = enabledSettings.stream()
                .map(s -> mapToPaymentMethodDto(s, primary != null && s.getId().equals(primary.getId()), currentInvoice))
                .toList();

        String razorpayKeyId = enabledSettings.stream()
                .filter(s -> "RAZORPAY".equalsIgnoreCase(s.getProvider()))
                .map(WorkspacePaymentSettings::getKeyId)
                .findFirst()
                .orElse(null);

        List<PublicInvoiceItemDto> itemDtos = invoice.getItems().stream()
                .map(item -> new PublicInvoiceItemDto(
                        item.getProductName(),
                        item.getProductPrice(),
                        item.getQuantity(),
                        item.getTotal()
                ))
                .toList();

        boolean hasPdf = invoice.getPdfLocation() != null || invoice.getPdfStatus() != null;

        String customerPhone = customer != null
                ? ((customer.getPhoneCode() != null ? customer.getPhoneCode().getCode() + " " : "") + (customer.getMobile() != null ? customer.getMobile() : "")).trim()
                : null;

        return new PublicInvoiceDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getTotal(),
                invoice.getTax(),
                invoice.getDiscount(),
                invoice.getNetTotal(),
                "INR",
                "₹",
                invoice.getStatus(),
                hasPdf,

                workspace.getId(),
                workspace.getName(),
                null, // logoUrl
                null, // billingAddress
                null, // taxComplianceName
                null, // gstNumber
                null, // panNumber

                customer != null ? customer.getFullName() : null,
                customer != null ? customer.getEmail() : null,
                customerPhone,
                null, // customerBillingAddress

                itemDtos,
                paymentEnabled,
                primaryPaymentMethod,
                paymentMethods,
                razorpayKeyId
        );
    }

    @Transactional
    public ResponseEntity<byte[]> getPublicInvoicePdf(String token) {
        Invoice invoice = tokenService.resolveInvoiceByToken(token);
        return invoiceService.getInvoicePdf(invoice.getWorkspace().getId(), invoice.getId());
    }

    @Transactional
    public CreateRazorpayOrderResponse createRazorpayOrder(String token) {
        Invoice invoice = tokenService.resolveInvoiceByToken(token);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("This invoice has already been paid");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("This invoice has been cancelled");
        }
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new BadRequestException("Draft invoices cannot accept payments");
        }

        Workspace workspace = invoice.getWorkspace();
        WorkspacePaymentSettings settings = paymentSettingsRepository.findByWorkspaceIdAndProvider(workspace.getId(), "RAZORPAY")
                .orElseThrow(() -> new PaymentGatewayException("Online payments are not configured by this biller"));

        if (!settings.isEnabled()) {
            throw new PaymentGatewayException("Online payments are currently disabled by this biller");
        }

        RazorpayClient client = razorpayClientFactory.getClient(workspace.getId());

        // Amount in smallest unit (paise for INR)
        long amountInPaise = invoice.getNetTotal().multiply(BigDecimal.valueOf(100)).longValue();
        String currency = "INR";

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : invoice.getId().toString());

            JSONObject notes = new JSONObject();
            notes.put("invoice_id", invoice.getId().toString());
            notes.put("workspace_id", workspace.getId().toString());
            orderRequest.put("notes", notes);

            Order order = client.orders.create(orderRequest);
            String orderId = order.get("id");

            Payment payment = Payment.builder()
                    .invoice(invoice)
                    .workspace(workspace)
                    .amount(invoice.getNetTotal())
                    .status(PaymentStatus.PENDING)
                    .provider("RAZORPAY")
                    .orderId(orderId)
                    .currency(currency)
                    .notes(notes.toString())
                    .build();

            paymentRepository.save(payment);

            Customer customer = invoice.getCustomer();
            String customerName = customer != null ? customer.getFullName() : "";
            String customerEmail = customer != null ? customer.getEmail() : "";
            String customerPhone = customer != null ? customer.getMobile() : "";

            return new CreateRazorpayOrderResponse(
                    orderId,
                    settings.getKeyId(),
                    amountInPaise,
                    currency,
                    invoice.getInvoiceNumber(),
                    customerName,
                    customerEmail,
                    customerPhone
            );
        } catch (PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create Razorpay order for invoice {}: {}", invoice.getId(), e.getMessage());
            throw new PaymentGatewayException("Failed to initiate payment: " + e.getMessage(), e);
        }
    }

    @Transactional
    public RazorpayPaymentVerifyResponse verifyPayment(String token, RazorpayPaymentVerifyRequest request) {
        Invoice invoice = tokenService.resolveInvoiceByToken(token);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return new RazorpayPaymentVerifyResponse(true, "Invoice already paid", request.razorpayPaymentId(), "PAID");
        }

        Workspace workspace = invoice.getWorkspace();
        WorkspacePaymentSettings settings = paymentSettingsRepository.findByWorkspaceIdAndProvider(workspace.getId(), "RAZORPAY")
                .orElseThrow(() -> new PaymentGatewayException("Payment gateway not configured for this biller"));

        String secret = razorpayClientFactory.getDecryptedKeySecret(settings);

        boolean signatureValid = verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature(),
                secret
        );

        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(request.razorpayOrderId());

        if (!signatureValid) {
            paymentOpt.ifPresent(p -> {
                p.setStatus(PaymentStatus.FAILED);
                p.setErrorCode("SIGNATURE_VERIFICATION_FAILED");
                p.setErrorDescription("Razorpay signature verification failed");
                paymentRepository.save(p);
            });
            throw new BadRequestException("Invalid payment signature");
        }

        Payment payment = paymentOpt.orElseGet(() ->
                Payment.builder()
                        .invoice(invoice)
                        .workspace(workspace)
                        .amount(invoice.getNetTotal())
                        .orderId(request.razorpayOrderId())
                        .provider("RAZORPAY")
                        .currency("INR")
                        .build()
        );

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setProviderPaymentId(request.razorpayPaymentId());
        payment.setSignature(request.razorpaySignature());
        payment.setCompletedAt(Instant.now());
        paymentRepository.save(payment);

        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        log.info("Successfully verified and recorded payment {} for invoice {}", payment.getId(), invoice.getId());

        return new RazorpayPaymentVerifyResponse(
                true,
                "Payment successfully verified",
                request.razorpayPaymentId(),
                "PAID"
        );
    }

    @Transactional
    public NotifyOfflinePaymentResponse notifyOfflinePayment(String token, NotifyOfflinePaymentRequest request) {
        Invoice invoice = tokenService.resolveInvoiceByToken(token);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("This invoice has already been paid");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("This invoice has been cancelled");
        }
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new BadRequestException("Draft invoices cannot accept payments");
        }

        String provider = request.provider().toUpperCase();
        if (!provider.equals("UPI") && !provider.equals("BANK_TRANSFER")) {
            throw new BadRequestException("Invalid payment method for offline payment notification");
        }

        boolean isConfigured = paymentSettingsRepository.findByWorkspaceIdAndProvider(invoice.getWorkspace().getId(), provider)
                .map(WorkspacePaymentSettings::isEnabled)
                .orElse(false);

        if (!isConfigured) {
            throw new BadRequestException(provider + " payment is not enabled for this biller");
        }

        String ref = request.transactionReference().trim();

        Payment payment = Payment.builder()
                .invoice(invoice)
                .workspace(invoice.getWorkspace())
                .amount(invoice.getNetTotal())
                .status(PaymentStatus.PENDING)
                .provider(provider)
                .paymentMethod(provider)
                .providerPaymentId(ref)
                .currency("INR")
                .notes(request.notes() != null && !request.notes().isBlank()
                        ? request.notes().trim()
                        : "Customer submitted offline payment reference: " + ref)
                .build();

        paymentRepository.save(payment);

        log.info("Recorded customer offline payment proof for invoice {}: provider={}, ref={}",
                invoice.getId(), provider, ref);

        return new NotifyOfflinePaymentResponse(
                true,
                "Payment reference received! The biller will verify and update the invoice status shortly.",
                ref
        );
    }

    private PaymentMethodDto mapToPaymentMethodDto(WorkspacePaymentSettings settings, boolean isPrimary, Invoice invoice) {
        String provider = settings.getProvider();
        if ("RAZORPAY".equalsIgnoreCase(provider)) {
            return new PaymentMethodDto(
                    "RAZORPAY",
                    isPrimary,
                    settings.getKeyId(),
                    null, null, null,
                    null, null, null, null, null, null, null
            );
        } else if ("UPI".equalsIgnoreCase(provider)) {
            String payee = settings.getPayeeName() != null && !settings.getPayeeName().isBlank()
                    ? settings.getPayeeName()
                    : invoice.getWorkspace().getName();
            String upiUri = buildUpiUri(
                    settings.getUpiId(),
                    payee,
                    invoice.getNetTotal(),
                    invoice.getInvoiceNumber()
            );
            return new PaymentMethodDto(
                    "UPI",
                    isPrimary,
                    null,
                    settings.getUpiId(),
                    payee,
                    upiUri,
                    null, null, null, null, null, null, null
            );
        } else if ("BANK_TRANSFER".equalsIgnoreCase(provider)) {
            return new PaymentMethodDto(
                    "BANK_TRANSFER",
                    isPrimary,
                    null,
                    null, null, null,
                    settings.getBankAccountName(),
                    settings.getBankAccountNumber(),
                    settings.getBankIfscCode(),
                    settings.getBankName(),
                    settings.getBankBranch(),
                    settings.getBankSwiftCode(),
                    settings.getInstructions()
            );
        }
        return new PaymentMethodDto(
                provider, isPrimary,
                null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    private String buildUpiUri(String upiId, String payeeName, BigDecimal amount, String invoiceNumber) {
        if (upiId == null || upiId.isBlank()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("upi://pay?");
        sb.append("pa=").append(urlEncode(upiId.trim()));
        if (payeeName != null && !payeeName.isBlank()) {
            sb.append("&pn=").append(urlEncode(payeeName.trim()));
        }
        if (amount != null) {
            sb.append("&am=").append(String.format(java.util.Locale.US, "%.2f", amount));
        }
        String note = invoiceNumber != null && !invoiceNumber.isBlank()
                ? "Invoice " + invoiceNumber.trim()
                : "Invoice Payment";
        sb.append("&tn=").append(urlEncode(note));
        sb.append("&cu=INR");
        return sb.toString();
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
        } catch (Exception e) {
            return value;
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String signature, String secret) {
        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hash);

            return MessageDigest.isEqual(
                    generatedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Signature verification algorithm error: {}", e.getMessage());
            return false;
        }
    }
}
