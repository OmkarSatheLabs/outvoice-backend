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

    @GetMapping
    public List<InvoiceResponse> getInvoices(@PathVariable UUID workspaceId) {
        return invoiceService.getInvoices(workspaceId);
    }

    @GetMapping("/{invoiceId}")
    public InvoiceResponse getInvoice(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
        return invoiceService.getInvoice(workspaceId, invoiceId);
    }

//    @GetMapping("/{invoiceId}/pdf")
//    public ResponseEntity<byte[]> sendInvoice(@PathVariable UUID workspaceId, @PathVariable UUID invoiceId) {
//        return invoiceService.sendInvoice(invoiceId);
//    }
}
