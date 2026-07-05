package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.workspace.customer.dto.CreateCustomerRequest;
import com.omkarsathe.outvoice.workspace.customer.dto.CustomerResponse;
import com.omkarsathe.outvoice.workspace.customer.dto.SearchEntityResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{workspaceId}/customers")
    public List<CustomerResponse> getCustomers(@PathVariable String workspaceId) {
        return customerService.getCustomers(workspaceId);
    }

    @GetMapping("/{workspaceId}/customers/search")
    public List<SearchEntityResponse> searchEntities(@PathVariable String workspaceId, @RequestParam SearchQueryTypeEnum queryType, @RequestParam String emailOrPhoneCodeId, @RequestParam String mobile) {
        return customerService.searchEntities(workspaceId, queryType, emailOrPhoneCodeId, mobile);
    }

    @PostMapping("/{workspaceId}/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@PathVariable String workspaceId, @Valid @RequestBody CreateCustomerRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return customerService.createCustomer(workspaceId, request, userId);
    }
}
