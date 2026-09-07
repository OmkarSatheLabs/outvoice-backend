package com.omkarsathe.outvoice.workspace.invoice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/invoices/{token}")
@RequiredArgsConstructor
public class PublicInvoiceController {

    @GetMapping
    public void viewInvoice(@PathVariable String token) {}

    @GetMapping("/pdf")
    public void viewPdf(@PathVariable String token) {}

    @PostMapping("/payment")
    public void makePayment(@PathVariable String token) {}
}
