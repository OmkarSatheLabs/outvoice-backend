package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.common.exception.BadRequestException;
import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerResponseMapper customerResponseMapper;
    private final WorkspaceRepository workspaceRepository;
    private final PhoneCodeRepository phoneCodeRepository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomers(UUID workspaceId) {
        return customerRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(customerResponseMapper::toResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse createCustomer(UUID workspaceId, CreateCustomer request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workspace not found: " + workspaceId
                        ));

        List<Customer> customers = workspace.getCustomers();

        boolean emailExists = request.email() != null
                && customers.stream()
                .anyMatch(customer ->
                        customer.getEmail() != null
                                && customer.getEmail().equalsIgnoreCase(request.email())
                );

        if (emailExists) {
            throw new BadRequestException(
                    "A customer with this email address is already part of this workspace."
            );
        }

        boolean mobileExists = request.mobile() != null
                && request.phoneCode() != null
                && customers.stream()
                .anyMatch(customer ->
                        customer.getPhoneCode() != null
                                && customer.getPhoneCode().getCode()
                                .equals(request.phoneCode())
                                && customer.getMobile() != null
                                && customer.getMobile().equals(request.mobile())
                );

        if (mobileExists) {
            throw new BadRequestException(
                    "A customer with this mobile number is already part of this workspace."
            );
        }

        PhoneCode phoneCode = phoneCodeRepository.findByCode(request.phoneCode());

        Customer customer = Customer.builder()
                .workspace(workspace)
                .email(request.email())
                .phoneCode(phoneCode)
                .mobile(request.mobile())
                .fullName(request.fullName())
                .build();

        return customerResponseMapper.toResponse(customerRepository.save(customer));
    }
}
