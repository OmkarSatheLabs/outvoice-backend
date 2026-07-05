package com.omkarsathe.outvoice.workspace.invoice;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceReminderController {

    private final InvoiceService invoiceService;

    @PostMapping("/{invoiceId}/remind")
    public boolean sendReminder(@PathVariable UUID invoiceId) {
        return invoiceService.sendReminder(invoiceId);
    }
}
