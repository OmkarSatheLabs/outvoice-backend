package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.workspace.invoice.dto.CreateInvoiceRequest;
import com.omkarsathe.outvoice.workspace.invoice.dto.InvoiceResponse;
import com.omkarsathe.outvoice.workspace.invoice.dto.InvoiceSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{id}/invoices")
    public List<InvoiceResponse> getInvoices(@PathVariable UUID id) {
        return invoiceService.getInvoices(id);
    }

    @GetMapping("/{id}/invoices/summary")
    public InvoiceSummaryResponse getInvoicesSummary(@PathVariable UUID id) {
        return invoiceService.getInvoiceSummary(id);
    }

    @PostMapping("/{workspaceId}/invoices")
    public InvoiceResponse createInvoice(
            @PathVariable UUID workspaceId,
            @RequestBody CreateInvoiceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.fromString(userDetails.getUsername());
        return invoiceService.createInvoice(workspaceId, userId, request);
    }
}
