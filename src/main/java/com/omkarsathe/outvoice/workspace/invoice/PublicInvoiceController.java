package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.workspace.invoice.dto.CreateRazorpayOrderResponse;
import com.omkarsathe.outvoice.workspace.invoice.dto.NotifyOfflinePaymentRequest;
import com.omkarsathe.outvoice.workspace.invoice.dto.NotifyOfflinePaymentResponse;
import com.omkarsathe.outvoice.workspace.invoice.dto.PublicInvoiceDto;
import com.omkarsathe.outvoice.workspace.invoice.dto.RazorpayPaymentVerifyRequest;
import com.omkarsathe.outvoice.workspace.invoice.dto.RazorpayPaymentVerifyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/invoices/{token}")
@RequiredArgsConstructor
public class PublicInvoiceController {

    private final PublicInvoiceService publicInvoiceService;

    @GetMapping
    public ResponseEntity<PublicInvoiceDto> viewInvoice(@PathVariable String token) {
        return ResponseEntity.ok(publicInvoiceService.getPublicInvoice(token));
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> viewPdf(@PathVariable String token) {
        return publicInvoiceService.getPublicInvoicePdf(token);
    }

    @PostMapping("/payments/razorpay/create-order")
    public ResponseEntity<CreateRazorpayOrderResponse> createPaymentOrder(@PathVariable String token) {
        return ResponseEntity.ok(publicInvoiceService.createRazorpayOrder(token));
    }

    @PostMapping("/payments/razorpay/verify")
    public ResponseEntity<RazorpayPaymentVerifyResponse> verifyPayment(
            @PathVariable String token,
            @Valid @RequestBody RazorpayPaymentVerifyRequest request
    ) {
        return ResponseEntity.ok(publicInvoiceService.verifyPayment(token, request));
    }

    @PostMapping("/payments/notify-offline")
    public ResponseEntity<NotifyOfflinePaymentResponse> notifyOfflinePayment(
            @PathVariable String token,
            @Valid @RequestBody NotifyOfflinePaymentRequest request
    ) {
        return ResponseEntity.ok(publicInvoiceService.notifyOfflinePayment(token, request));
    }
}
