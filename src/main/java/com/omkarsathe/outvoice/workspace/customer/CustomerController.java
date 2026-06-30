package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.workspace.customer.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{id}/customers")
    public List<CustomerResponse> getInvoices(@PathVariable String id) {
        return customerService.getCustomers(id);
    }
}
