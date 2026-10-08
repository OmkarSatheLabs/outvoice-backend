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
    public InvoiceResponse create(@PathVariable UUID workspaceId, @Valid @RequestBody CreateInvoice request) {
        return invoiceService.create(workspaceId, request);
    }

    @PutMapping("/{invoiceId}")
    public InvoiceResponse update(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId, @Valid @RequestBody UpdateInvoice request) {
        return invoiceService.update(workspaceId, invoiceId, request);
    }

    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<?> delete(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.delete(workspaceId, invoiceId);
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
