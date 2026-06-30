package com.omkarsathe.outvoice.workspace.invoice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class InvoiceController {

    @GetMapping("/{id}/invoices")
    public String getInvoices(@PathVariable String id) {
        return "Hello World! " + id;
    }

    @GetMapping("/{id}/invoices/summary")
    public String getInvoicesSummary(@PathVariable String id) {
        return "Hello World! " + id;
    }
}
