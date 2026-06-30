package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.workspace.customer.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<CustomerResponse> getCustomers(String id) {
        UUID workspaceId = UUID.fromString(id);
        return customerRepository.findByWorkspaceId(workspaceId).stream()
                .map(customer -> CustomerResponse.builder()
                        .id(customer.getId())
                        .name(customer.getName())
                        .email(customer.getEmail())
                        .phoneCodeId(customer.getPhoneCode() != null ? customer.getPhoneCode().getId() : null)
                        .phoneCode(customer.getPhoneCode() != null ? customer.getPhoneCode().getCode() : null)
                        .mobile(customer.getMobile())
                        .companyName(customer.getCompanyName())
                        .billingAddress(customer.getBillingAddress())
                        .taxNumber(customer.getTaxNumber())
                        .build())
                .collect(Collectors.toList());
    }
}
