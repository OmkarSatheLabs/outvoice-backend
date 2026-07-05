package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.phone.PhoneCodeRepository;
import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.user.UserRepository;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceEntity;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
import com.omkarsathe.outvoice.workspace.*;
import com.omkarsathe.outvoice.workspace.customer.dto.SearchEntityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserWorkspaceRepository userWorkspaceRepository;

    @Mock
    private PhoneCodeRepository phoneCodeRepository;

    @InjectMocks
    private CustomerService customerService;

    private UUID workspaceId;
    private String workspaceIdStr;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        workspaceIdStr = workspaceId.toString();
    }

    @Test
    void searchEntities_shouldReturnEmptyList_whenQueryIsTooShort() {
        List<SearchEntityResponse> results = customerService.searchEntities(workspaceIdStr, SearchQueryTypeEnum.EMAIL, "a", "");
        assertThat(results).isEmpty();

        results = customerService.searchEntities(workspaceIdStr, SearchQueryTypeEnum.EMAIL, "   ", "");
        assertThat(results).isEmpty();
    }

    @Test
    void searchEntities_shouldFilterExistingCustomersAndAssociatedUsers() {
        // Arrange
        String query = "test";

        // Mocks for users
        UUID existingCustomerUserId = UUID.randomUUID();
        UUID associatedUserId = UUID.randomUUID();
        UUID normalUserId = UUID.randomUUID();

        UserEntity existingCustomerUser = UserEntity.builder().id(existingCustomerUserId).fullName("Existing Cust UserEntity").email("cust@test.com").build();
        UserEntity associatedUser = UserEntity.builder().id(associatedUserId).fullName("Associated UserEntity").email("assoc@test.com").build();
        UserEntity normalUser = UserEntity.builder().id(normalUserId).fullName("Normal UserEntity").email("normal@test.com").build();

        // Set up existing customers
        CustomerEntity customerWithUser = CustomerEntity.builder()
                .id(UUID.randomUUID())
                .user(existingCustomerUser)
                .build();
        when(customerRepository.findByWorkspaceId(workspaceId))
                .thenReturn(List.of(customerWithUser));

        // Set up associated users
        UserWorkspaceEntity userWorkspaceRelation = UserWorkspaceEntity.builder()
                .user(associatedUser)
                .build();
        when(userWorkspaceRepository.findByWorkspaceId(workspaceId))
                .thenReturn(List.of(userWorkspaceRelation));

        // Set up search results
        when(userRepository.searchUsers(query))
                .thenReturn(List.of(existingCustomerUser, associatedUser, normalUser));

        // Act
        List<SearchEntityResponse> results = customerService.searchEntities(workspaceIdStr, SearchQueryTypeEnum.EMAIL, query, "");

        // Assert
        // Should only contain normalUser
        assertThat(results).hasSize(1);

        SearchEntityResponse first = results.get(0);
        assertThat(first.getId()).isEqualTo(normalUserId);
        assertThat(first.getCustomerName()).isEqualTo("Normal UserEntity");
    }

    @Test
    void searchEntities_shouldMaskMobile_whenSearchingByEmail() {
        String query = "test";
        UUID normalUserId = UUID.randomUUID();
        UserEntity normalUser = UserEntity.builder()
                .id(normalUserId)
                .fullName("Normal User")
                .email("normal@test.com")
                .mobile("9876543210")
                .build();

        when(customerRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of());
        when(userWorkspaceRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of());
        when(userRepository.searchUsers(query)).thenReturn(List.of(normalUser));

        List<SearchEntityResponse> results = customerService.searchEntities(workspaceIdStr, SearchQueryTypeEnum.EMAIL, query, "");

        assertThat(results).hasSize(1);
        SearchEntityResponse first = results.get(0);
        assertThat(first.getMobile()).isEqualTo("******3210");
    }

    @Test
    void searchEntities_shouldMaskEmail_whenSearchingByMobile() {
        String phoneCodeIdStr = UUID.randomUUID().toString();
        String mobileQuery = "9876543210";
        UUID normalUserId = UUID.randomUUID();
        UserEntity normalUser = UserEntity.builder()
                .id(normalUserId)
                .fullName("Normal User")
                .email("omkarsathe@test.com")
                .mobile(mobileQuery)
                .build();

        when(customerRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of());
        when(userWorkspaceRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of());
        when(userRepository.findAllByPhoneCodeIdAndMobileContaining(any(UUID.class), any(String.class)))
                .thenReturn(List.of(normalUser));

        List<SearchEntityResponse> results = customerService.searchEntities(workspaceIdStr, SearchQueryTypeEnum.MOBILE, phoneCodeIdStr, mobileQuery);

        assertThat(results).hasSize(1);
        SearchEntityResponse first = results.get(0);
        assertThat(first.getEmail()).isEqualTo("omk*******@test.com");
    }
}
