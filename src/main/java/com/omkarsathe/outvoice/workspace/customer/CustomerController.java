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
    public CustomerResponse createCustomer(@PathVariable UUID workspaceId, @Valid @RequestBody CreateCustomer request) {
        return customerService.createCustomer(workspaceId, request);
    }

    @GetMapping("/workspaces/{workspaceId}/customers")
    public List<CustomerResponse> getCustomers(@PathVariable UUID workspaceId) {
        return customerService.getCustomers(workspaceId);
    }

    @GetMapping("/workspaces/{workspaceId}/customers/{customerId}")
    public CustomerResponse getCustomer(
            @PathVariable UUID workspaceId,
            @PathVariable UUID customerId
    ) {
        return customerService.getCustomer(workspaceId, customerId);
    }

    @PutMapping("/workspaces/{workspaceId}/customers/{customerId}")
    public CustomerResponse updateCustomer(
            @PathVariable UUID workspaceId,
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomer request
    ) {
        return customerService.updateCustomer(workspaceId, customerId, request);
    }

    @DeleteMapping("/workspaces/{workspaceId}/customers/{customerId}")
    public org.springframework.http.ResponseEntity<Void> deleteCustomer(
            @PathVariable UUID workspaceId,
            @PathVariable UUID customerId
    ) {
        customerService.deleteCustomer(workspaceId, customerId);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
