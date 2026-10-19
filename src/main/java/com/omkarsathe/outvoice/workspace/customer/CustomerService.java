package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.audit.log.AuditAction;
import com.omkarsathe.outvoice.audit.log.AuditResourceType;
import com.omkarsathe.outvoice.audit.log.AuditService;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerResponseMapper customerResponseMapper;
    private final WorkspaceRepository workspaceRepository;
    private final PhoneCodeRepository phoneCodeRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomers(UUID workspaceId) {
        return customerRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId)
                .stream()
                .map(customerResponseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID workspaceId, UUID customerId) {
        Customer customer = customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        return customerResponseMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(UUID workspaceId, CreateCustomer request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workspace not found: " + workspaceId
                        ));

        List<Customer> activeCustomers = customerRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId);

        boolean emailExists = request.email() != null
                && !request.email().isBlank()
                && activeCustomers.stream()
                .filter(customer -> customer.getDeletedAt() == null)
                .anyMatch(customer ->
                        customer.getEmail() != null
                                && customer.getEmail().equalsIgnoreCase(request.email().trim())
                );

        if (emailExists) {
            throw new BadRequestException(
                    "A customer with this email address is already part of this workspace."
            );
        }

        boolean mobileExists = request.mobile() != null
                && !request.mobile().isBlank()
                && request.phoneCode() != null
                && !request.phoneCode().isBlank()
                && activeCustomers.stream()
                .filter(customer -> customer.getDeletedAt() == null)
                .anyMatch(customer ->
                        customer.getPhoneCode() != null
                                && customer.getPhoneCode().getCode().equals(request.phoneCode().trim())
                                && customer.getMobile() != null
                                && customer.getMobile().equals(request.mobile().trim())
                );

        if (mobileExists) {
            throw new BadRequestException(
                    "A customer with this mobile number is already part of this workspace."
            );
        }

        PhoneCode phoneCode = (request.phoneCode() != null && !request.phoneCode().isBlank())
                ? phoneCodeRepository.findByCode(request.phoneCode().trim())
                : null;

        String trimmedAddress = (request.address() != null && !request.address().isBlank()) ? request.address().trim() : null;
        String trimmedEmail = (request.email() != null && !request.email().isBlank()) ? request.email().trim().toLowerCase() : null;
        String trimmedMobile = (request.mobile() != null && !request.mobile().isBlank()) ? request.mobile().trim() : null;

        Customer customer = Customer.builder()
                .workspace(workspace)
                .email(trimmedEmail)
                .phoneCode(phoneCode)
                .mobile(trimmedMobile)
                .fullName(request.fullName().trim())
                .address(trimmedAddress)
                .build();

        Customer saved = customerRepository.save(customer);

        auditService.log(
                workspaceId,
                AuditAction.CREATE,
                AuditResourceType.CUSTOMER,
                saved.getId(),
                "Created customer " + saved.getFullName(),
                Map.of(
                        "fullName", saved.getFullName() != null ? saved.getFullName() : "",
                        "email", saved.getEmail() != null ? saved.getEmail() : "",
                        "mobile", saved.getMobile() != null ? saved.getMobile() : "",
                        "address", saved.getAddress() != null ? saved.getAddress() : ""
                )
        );

        return customerResponseMapper.toResponse(saved);
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID workspaceId, UUID customerId, UpdateCustomer request) {
        Customer customer = customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        List<Customer> otherActiveCustomers = customerRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId)
                .stream()
                .filter(c -> !c.getId().equals(customerId))
                .toList();

        if (request.email() != null && !request.email().isBlank()) {
            boolean emailExists = otherActiveCustomers.stream()
                    .anyMatch(c -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(request.email().trim()));
            if (emailExists) {
                throw new BadRequestException("A customer with this email address is already part of this workspace.");
            }
        }

        if (request.mobile() != null && !request.mobile().isBlank() && request.phoneCode() != null && !request.phoneCode().isBlank()) {
            boolean mobileExists = otherActiveCustomers.stream()
                    .anyMatch(c -> c.getPhoneCode() != null && c.getPhoneCode().getCode().equals(request.phoneCode().trim())
                            && c.getMobile() != null && c.getMobile().equals(request.mobile().trim()));
            if (mobileExists) {
                throw new BadRequestException("A customer with this mobile number is already part of this workspace.");
            }
        }

        PhoneCode phoneCode = (request.phoneCode() != null && !request.phoneCode().isBlank())
                ? phoneCodeRepository.findByCode(request.phoneCode().trim())
                : null;

        String trimmedAddress = (request.address() != null && !request.address().isBlank()) ? request.address().trim() : null;
        String trimmedEmail = (request.email() != null && !request.email().isBlank()) ? request.email().trim().toLowerCase() : null;
        String trimmedMobile = (request.mobile() != null && !request.mobile().isBlank()) ? request.mobile().trim() : null;

        customer.setFullName(request.fullName().trim());
        customer.setEmail(trimmedEmail);
        customer.setPhoneCode(phoneCode);
        customer.setMobile(trimmedMobile);
        customer.setAddress(trimmedAddress);

        Customer saved = customerRepository.save(customer);

        auditService.log(
                workspaceId,
                AuditAction.UPDATE,
                AuditResourceType.CUSTOMER,
                saved.getId(),
                "Updated customer " + saved.getFullName(),
                Map.of(
                        "fullName", saved.getFullName() != null ? saved.getFullName() : "",
                        "email", saved.getEmail() != null ? saved.getEmail() : "",
                        "mobile", saved.getMobile() != null ? saved.getMobile() : "",
                        "address", saved.getAddress() != null ? saved.getAddress() : ""
                )
        );

        return customerResponseMapper.toResponse(saved);
    }

    @Transactional
    public void deleteCustomer(UUID workspaceId, UUID customerId) {
        Customer customer = customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        customer.setDeletedAt(java.time.Instant.now());
        customerRepository.save(customer);

        auditService.log(
                workspaceId,
                AuditAction.DELETE,
                AuditResourceType.CUSTOMER,
                customer.getId(),
                "Deleted customer " + customer.getFullName(),
                Map.of(
                        "fullName", customer.getFullName() != null ? customer.getFullName() : "",
                        "deletedAt", customer.getDeletedAt().toString()
                )
        );
    }
}
