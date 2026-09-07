//package com.omkarsathe.outvoice.workspace.invoice;
//
//import com.omkarsathe.outvoice.common.exception.ForbiddenException;
//import com.omkarsathe.outvoice.workspace.CurrentWorkspaceUser;
//import com.omkarsathe.outvoice.workspace.Permission;
//import com.omkarsathe.outvoice.workspace.WorkspacePrincipal;
//import com.omkarsathe.outvoice.workspace.invoice.dto.CreateInvoiceRequest;
//import com.omkarsathe.outvoice.workspace.invoice.dto.CreateInvoiceResponse;
//import com.omkarsathe.outvoice.workspace.invoice.dto.InvoiceResponse;
//import com.omkarsathe.outvoice.workspace.invoice.dto.InvoiceSummaryResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/workspaces")
//@RequiredArgsConstructor
//public class WorkspaceInvoiceController {
//
//    private final WorkspaceInvoiceService invoiceService;
//
//    @GetMapping("/{id}/invoices")
//    public List<CreateInvoiceResponse> getInvoices(@PathVariable UUID id) {
//        return invoiceService.getInvoices(id);
//    }
//
//    @GetMapping("/{id}/invoices/summary")
//    public InvoiceSummaryResponse getInvoicesSummary(@PathVariable UUID id) {
//        return invoiceService.getInvoiceSummary(id);
//    }
//
////    @PostMapping("/{workspaceId}/invoices")
////    public CreateInvoiceResponse createInvoice(
////            @RequestBody CreateInvoiceRequest request,
////            @CurrentWorkspaceUser WorkspacePrincipal principal) {
////
////        if (principal.hasPermission(Permission.INVOICES_CREATE)) {
////            throw new ForbiddenException("PERMISSION_DENIED", "INVOICES_CREATE");
////        }
////
////        return invoiceService.save(request);
////    }
//
//    @GetMapping("/{workspaceId}/invoices/{invoiceId}")
//    public InvoiceResponse getInvoice(
//            @PathVariable UUID workspaceId,
//            @PathVariable UUID invoiceId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        UUID userId = UUID.fromString(userDetails.getUsername());
//        return invoiceService.getInvoice(workspaceId, invoiceId, userId);
//    }
//}
