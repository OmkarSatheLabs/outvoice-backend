package com.omkarsathe.outvoice.workspace.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/workspaces/{workspaceId}/customers")
    public CustomerResponse create(@PathVariable UUID workspaceId, @Valid @RequestBody CreateCustomer request) {
        return customerService.create(workspaceId, request);
    }

    @GetMapping("/workspaces/{workspaceId}/customers")
    public List<CustomerResponse> getCustomers(@PathVariable UUID workspaceId) {
        return customerService.getCustomers(workspaceId);
    }
}
