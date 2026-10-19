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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerResponseMapper customerResponseMapper;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private PhoneCodeRepository phoneCodeRepository;

    @Mock
    private AuditService auditService;

    private CustomerService customerService;

    private final UUID workspaceId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(
                customerRepository,
                customerResponseMapper,
                workspaceRepository,
                phoneCodeRepository,
                auditService
        );

        workspace = Workspace.builder()
                .id(workspaceId)
                .name("Acme Corp")
                .build();
    }

    @Test
    void getCustomers_OnlyReturnsActiveNonDeletedCustomers() {
        when(customerRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId))
                .thenReturn(List.of());

        List<CustomerResponse> responses = customerService.getCustomers(workspaceId);

        assertNotNull(responses);
        verify(customerRepository).findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId);
    }

    @Test
    void getCustomer_WhenActiveCustomerExists_ReturnsResponse() {
        Customer customer = Customer.builder()
                .id(customerId)
                .workspace(workspace)
                .fullName("John Doe")
                .email("john@example.com")
                .address("123 Tech Park, Bangalore")
                .build();

        CustomerResponse expected = new CustomerResponse(
                customerId,
                "John Doe",
                "john@example.com",
                null,
                null,
                "123 Tech Park, Bangalore",
                List.of(),
                null,
                null,
                null
        );

        when(customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId))
                .thenReturn(Optional.of(customer));
        when(customerResponseMapper.toResponse(customer)).thenReturn(expected);

        CustomerResponse result = customerService.getCustomer(workspaceId, customerId);

        assertNotNull(result);
        assertEquals("John Doe", result.fullName());
        assertEquals("123 Tech Park, Bangalore", result.address());
    }

    @Test
    void getCustomer_WhenNotFoundOrDeleted_ThrowsException() {
        when(customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                customerService.getCustomer(workspaceId, customerId));
    }

    @Test
    void createCustomer_SavesCustomerWithAddressAndLogsAudit() {
        CreateCustomer request = new CreateCustomer(
                "alice@example.com",
                "+91",
                "9876543210",
                "Alice Smith",
                "45 Innovation Hub, Mumbai"
        );

        PhoneCode phoneCode = new PhoneCode();
        phoneCode.setCode("+91");

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(customerRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId)).thenReturn(List.of());
        when(phoneCodeRepository.findByCode("+91")).thenReturn(phoneCode);

        Customer savedCustomer = Customer.builder()
                .id(customerId)
                .workspace(workspace)
                .fullName("Alice Smith")
                .email("alice@example.com")
                .phoneCode(phoneCode)
                .mobile("9876543210")
                .address("45 Innovation Hub, Mumbai")
                .build();

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(customerResponseMapper.toResponse(savedCustomer)).thenReturn(new CustomerResponse(
                customerId, "Alice Smith", "alice@example.com", null, "9876543210", "45 Innovation Hub, Mumbai", List.of(), null, null, null
        ));

        CustomerResponse response = customerService.createCustomer(workspaceId, request);

        assertNotNull(response);
        assertEquals("Alice Smith", response.fullName());
        assertEquals("45 Innovation Hub, Mumbai", response.address());

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertEquals("Alice Smith", captor.getValue().getFullName());
        assertEquals("45 Innovation Hub, Mumbai", captor.getValue().getAddress());

        verify(auditService).log(
                eq(workspaceId),
                eq(AuditAction.CREATE),
                eq(AuditResourceType.CUSTOMER),
                eq(customerId),
                contains("Created customer Alice Smith"),
                anyMap()
        );
    }

    @Test
    void updateCustomer_SuccessfullyUpdatesAllFieldsAndAddress() {
        Customer existing = Customer.builder()
                .id(customerId)
                .workspace(workspace)
                .fullName("Old Name")
                .email("old@example.com")
                .mobile("1234567890")
                .address("Old Address")
                .build();

        UpdateCustomer updateRequest = new UpdateCustomer(
                "new@example.com",
                "+1",
                "9876543210",
                "New Name",
                "New Address Suite 200"
        );

        PhoneCode newPhoneCode = new PhoneCode();
        newPhoneCode.setCode("+1");

        when(customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId))
                .thenReturn(Optional.of(existing));
        when(customerRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId))
                .thenReturn(List.of(existing));
        when(phoneCodeRepository.findByCode("+1")).thenReturn(newPhoneCode);
        when(customerRepository.save(existing)).thenReturn(existing);
        when(customerResponseMapper.toResponse(existing)).thenReturn(new CustomerResponse(
                customerId, "New Name", "new@example.com", null, "9876543210", "New Address Suite 200", List.of(), null, null, null
        ));

        CustomerResponse response = customerService.updateCustomer(workspaceId, customerId, updateRequest);

        assertNotNull(response);
        assertEquals("New Name", response.fullName());
        assertEquals("New Address Suite 200", response.address());

        assertEquals("New Name", existing.getFullName());
        assertEquals("new@example.com", existing.getEmail());
        assertEquals("9876543210", existing.getMobile());
        assertEquals(newPhoneCode, existing.getPhoneCode());
        assertEquals("New Address Suite 200", existing.getAddress());

        verify(auditService).log(
                eq(workspaceId),
                eq(AuditAction.UPDATE),
                eq(AuditResourceType.CUSTOMER),
                eq(customerId),
                contains("Updated customer New Name"),
                anyMap()
        );
    }

    @Test
    void updateCustomer_DuplicateEmailOnDifferentCustomer_ThrowsBadRequestException() {
        Customer existing = Customer.builder()
                .id(customerId)
                .workspace(workspace)
                .fullName("Customer A")
                .email("a@example.com")
                .build();

        UUID otherId = UUID.randomUUID();
        Customer other = Customer.builder()
                .id(otherId)
                .workspace(workspace)
                .fullName("Customer B")
                .email("duplicate@example.com")
                .build();

        UpdateCustomer request = new UpdateCustomer(
                "duplicate@example.com",
                null,
                null,
                "Customer A Updated",
                null
        );

        when(customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId))
                .thenReturn(Optional.of(existing));
        when(customerRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId))
                .thenReturn(List.of(existing, other));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                customerService.updateCustomer(workspaceId, customerId, request));

        assertTrue(ex.getMessage().contains("already part of this workspace"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomer_KeepingOwnEmail_SucceedsWithoutDuplicateError() {
        Customer existing = Customer.builder()
                .id(customerId)
                .workspace(workspace)
                .fullName("Customer A")
                .email("same@example.com")
                .build();

        UpdateCustomer request = new UpdateCustomer(
                "same@example.com",
                null,
                null,
                "Customer A Renamed",
                "New Address"
        );

        when(customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId))
                .thenReturn(Optional.of(existing));
        when(customerRepository.findAllByWorkspace_IdAndDeletedAtIsNull(workspaceId))
                .thenReturn(List.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);
        when(customerResponseMapper.toResponse(existing)).thenReturn(new CustomerResponse(
                customerId, "Customer A Renamed", "same@example.com", null, null, "New Address", List.of(), null, null, null
        ));

        CustomerResponse response = customerService.updateCustomer(workspaceId, customerId, request);

        assertNotNull(response);
        assertEquals("Customer A Renamed", existing.getFullName());
        verify(customerRepository).save(existing);
    }

    @Test
    void deleteCustomer_SetsDeletedAtTimestampAndAudits() {
        Customer existing = Customer.builder()
                .id(customerId)
                .workspace(workspace)
                .fullName("To Be Deleted")
                .email("delete@example.com")
                .build();

        when(customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId))
                .thenReturn(Optional.of(existing));

        customerService.deleteCustomer(workspaceId, customerId);

        assertNotNull(existing.getDeletedAt());
        verify(customerRepository).save(existing);
        verify(auditService).log(
                eq(workspaceId),
                eq(AuditAction.DELETE),
                eq(AuditResourceType.CUSTOMER),
                eq(customerId),
                contains("Deleted customer To Be Deleted"),
                anyMap()
        );
    }

    @Test
    void deleteCustomer_WhenCustomerNotFoundOrAlreadyDeleted_ThrowsResourceNotFoundException() {
        when(customerRepository.findByIdAndWorkspace_IdAndDeletedAtIsNull(customerId, workspaceId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                customerService.deleteCustomer(workspaceId, customerId));

        verify(customerRepository, never()).save(any());
    }
}
