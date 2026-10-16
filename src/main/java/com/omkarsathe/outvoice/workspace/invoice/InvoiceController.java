package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.workspace.invoice.dto.InvoiceShareResponse;
import com.omkarsathe.outvoice.workspace.invoice.dto.RecordPaymentRequest;
import com.omkarsathe.outvoice.workspace.payment.dto.PaymentDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public InvoiceResponse createInvoice(@PathVariable UUID workspaceId, @Valid @RequestBody CreateInvoice request) {
        return invoiceService.createInvoice(workspaceId, request);
    }

    @PutMapping("/{invoiceId}")
    public InvoiceResponse updateInvoice(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId, @Valid @RequestBody UpdateInvoice request) {
        return invoiceService.updateInvoice(workspaceId, invoiceId, request);
    }

    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<?> deleteInvoice(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.deleteInvoice(workspaceId, invoiceId);
    }

    @GetMapping
    public List<InvoiceResponse> getInvoices(@PathVariable UUID workspaceId) {
        return invoiceService.getInvoices(workspaceId);
    }

    @GetMapping("/summary")
    public InvoiceSummaryResponse getSummary(@PathVariable UUID workspaceId) {
        return invoiceService.getSummary(workspaceId);
    }

    @GetMapping("/{invoiceId}")
    public InvoiceResponse getInvoice(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.getInvoice(workspaceId, invoiceId);
    }

    @GetMapping("/{invoiceId}/pdf/status")
    public InvoicePdfStatusResponse getInvoicePdfStatus(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.getInvoicePdfStatus(workspaceId, invoiceId);
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.getInvoicePdf(workspaceId, invoiceId);
    }

    @PostMapping("/{invoiceId}/pdf/retry")
    public InvoicePdfStatusResponse retryInvoicePdf(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.retryInvoicePdf(workspaceId, invoiceId);
    }

    @GetMapping("/{invoiceId}/share")
    public InvoiceShareResponse getShareLink(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.getShareLink(workspaceId, invoiceId);
    }

    @PostMapping("/{invoiceId}/mark-paid")
    public InvoiceResponse markInvoiceAsPaid(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.markInvoiceAsPaid(workspaceId, invoiceId);
    }

    @PostMapping("/{invoiceId}/record-payment")
    public InvoiceResponse recordPayment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID invoiceId,
            @RequestBody RecordPaymentRequest request
    ) {
        return invoiceService.recordPayment(workspaceId, invoiceId, request);
    }

    @GetMapping("/{invoiceId}/payments")
    public List<PaymentDto> getInvoicePayments(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.getInvoicePayments(workspaceId, invoiceId);
    }
}
