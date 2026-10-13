package com.omkarsathe.outvoice.workspace.invoice;

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
}
