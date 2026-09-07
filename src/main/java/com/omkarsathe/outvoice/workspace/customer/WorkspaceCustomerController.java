//package com.omkarsathe.outvoice.workspace.customer;
//
//import com.omkarsathe.outvoice.common.exception.ForbiddenException;
//import com.omkarsathe.outvoice.workspace.CurrentWorkspaceUser;
//import com.omkarsathe.outvoice.workspace.Permission;
////import com.omkarsathe.outvoice.workspace.WorkspacePrincipal;
//import com.omkarsathe.outvoice.workspace.customer.dto.CreateCustomerRequest;
//import com.omkarsathe.outvoice.workspace.customer.dto.CreateWorkspaceCustomerResponseDto;
//import com.omkarsathe.outvoice.workspace.customer.dto.CustomerResponse;
//import com.omkarsathe.outvoice.workspace.customer.dto.SearchEntityResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/workspaces/{workspaceId}/customers")
//@RequiredArgsConstructor
//public class WorkspaceCustomerController {
//
//    private final WorkspaceCustomerService workspaceCustomerService;
//
//    @GetMapping()
//    public List<CustomerResponse> getCustomersByWorkspaceId(@PathVariable String workspaceId) {
//        return workspaceCustomerService.getCustomersByWorkspaceId(workspaceId);
//    }
//
//    @GetMapping("/search")
//    public List<SearchEntityResponse> searchCustomersByWorkspaceByCustomers(@PathVariable String workspaceId, @RequestParam SearchQueryTypeEnum queryType, @RequestParam String emailOrPhoneCodeId, @RequestParam String mobile) {
//        return workspaceCustomerService.searchEntities(workspaceId, queryType, emailOrPhoneCodeId, mobile);
//    }
//
////    @PostMapping()
////    @ResponseStatus(HttpStatus.CREATED)
////    public CreateWorkspaceCustomerResponseDto createWorkspaceCustomer(@PathVariable String workspaceId, @Valid @RequestBody CreateCustomerRequest request, @CurrentWorkspaceUser WorkspacePrincipal principal) {
////        if (principal.hasPermission(Permission.CUSTOMERS_CREATE)) {
////            throw new ForbiddenException("PERMISSION_DENIED", "CUSTOMERS_CREATE");
////        }
////
////        return workspaceCustomerService.createWorkspaceCustomer(UUID.fromString(workspaceId), request, principal.userId());
////    }
//}
