//package com.omkarsathe.outvoice.workspace.invoice;
//
//import com.omkarsathe.outvoice.user.UserRepository;
//import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
//import com.omkarsathe.outvoice.workspace.Workspace;
//import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
//import com.omkarsathe.outvoice.workspace.customer.WorkspaceCustomerEntity;
//import com.omkarsathe.outvoice.workspace.customer.WorkspaceCustomerRepository;
//import com.omkarsathe.outvoice.workspace.invoice.dto.CreateInvoiceRequest;
//import com.omkarsathe.outvoice.workspace.invoice.dto.CreateInvoiceResponse;
//import com.omkarsathe.outvoice.workspace.invoice.dto.InvoiceResponse;
//import com.omkarsathe.outvoice.workspace.invoice.dto.InvoiceSummaryResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class WorkspaceInvoiceService {
//
//    private final WorkspaceInvoiceRepository invoiceRepository;
//    private final WorkspaceRepository workspaceRepository;
//    private final UserWorkspaceRepository userWorkspaceRepository;
//    private final WorkspaceCustomerRepository workspaceCustomerRepository;
//    private final UserRepository userRepository;
//
//    @Transactional(readOnly = true)
//    public List<CreateInvoiceResponse> getInvoices(UUID workspaceId) {
//        return invoiceRepository.findByWorkspaceIdFetchCustomer(workspaceId).stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public InvoiceSummaryResponse getInvoiceSummary(UUID workspaceId) {
//        Workspace workspace = workspaceRepository.findById(workspaceId)
//                .orElseThrow(() -> new RuntimeException("Workspace not found"));
//
//        List<WorkspaceInvoiceEntity> invoices = invoiceRepository.findByWorkspaceIdFetchCustomer(workspaceId);
//
//        BigDecimal totalInvoiced = BigDecimal.ZERO;
//        BigDecimal totalPaid = BigDecimal.ZERO;
//        BigDecimal totalPending = BigDecimal.ZERO;
//        BigDecimal totalOverdue = BigDecimal.ZERO;
//
//        Map<String, Long> countByStatus = new HashMap<>();
//        for (WorkspaceInvoiceStatus status : WorkspaceInvoiceStatus.values()) {
//            countByStatus.put(status.name(), 0L);
//        }
//
//        for (WorkspaceInvoiceEntity inv : invoices) {
//            String statusStr = inv.getStatus().name();
//            countByStatus.put(statusStr, countByStatus.getOrDefault(statusStr, 0L) + 1);
//
//            BigDecimal amt = inv.getTotalAmount();
//            if (inv.getStatus() != WorkspaceInvoiceStatus.DRAFT) {
//                totalInvoiced = totalInvoiced.add(amt);
//            }
//            if (inv.getStatus() == WorkspaceInvoiceStatus.PAID) {
//                totalPaid = totalPaid.add(amt);
//            } else if (inv.getStatus() == WorkspaceInvoiceStatus.SENT) {
//                totalPending = totalPending.add(amt);
//            } else if (inv.getStatus() == WorkspaceInvoiceStatus.OVERDUE || inv.getStatus() == WorkspaceInvoiceStatus.DEFAULTED) {
//                totalOverdue = totalOverdue.add(amt);
//            }
//        }
//
////        String currencyCode = workspace.getCurrency() != null ? workspace.getCurrency().getCode() : "INR";
//
//        return InvoiceSummaryResponse.builder()
//                .totalInvoiced(totalInvoiced)
//                .totalPaid(totalPaid)
//                .totalPending(totalPending)
//                .totalOverdue(totalOverdue)
////                .currencyCode(currencyCode)
//                .countByStatus(countByStatus)
//                .build();
//    }
//
//    @Transactional
//    public CreateInvoiceResponse save(CreateInvoiceRequest request) {
//
//        // Create workspace invoice
//        //
//        WorkspaceInvoiceStatus status;
//        try {
//            status = WorkspaceInvoiceStatus.valueOf(request.getStatus().toUpperCase());
//        } catch (Exception e) {
//            status = WorkspaceInvoiceStatus.DRAFT;
//        }
//
//        BigDecimal taxRate = request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO;
//
//        WorkspaceInvoiceEntity invoice = WorkspaceInvoiceEntity.builder()
//                .invoiceNumber(request.getInvoiceNumber())
//                .currency(request.getCurrencyCode() != null ? request.getCurrencyCode() : "INR")
//                .status(status)
//                .issueDate(request.getIssueDate())
//                .dueDate(request.getDueDate())
//                .taxAmount(taxRate)
//                .build();
//
//        BigDecimal computedTotal = BigDecimal.ZERO;
//        if (request.getLineItems() != null && !request.getLineItems().isEmpty()) {
//            for (CreateInvoiceRequest.LineItemDto itemDto : request.getLineItems()) {
//                BigDecimal qty = itemDto.getQuantity() != null ? itemDto.getQuantity() : BigDecimal.ONE;
//                BigDecimal lineAmount = qty.multiply(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO);
//
//                WorkspaceInvoiceLineItemEntity item = WorkspaceInvoiceLineItemEntity.builder()
//                        .description(itemDto.getDescription())
//                        .quantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : BigDecimal.ONE)
//                        .unitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO)
//                        .lineTotal(lineAmount)
//                        .lineSubtotal(lineAmount)
//                        .build();
//
//                invoice.addLineItem(item);
//                computedTotal = computedTotal.add(lineAmount);
//            }
//
//            // Apply Tax: total = subtotal * (1 + taxRate/100)
//            BigDecimal taxMultiplier = BigDecimal.ONE.add(taxRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
//            computedTotal = computedTotal.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);
//            invoice.setTotalAmount(computedTotal);
//        } else {
//            // Fallback to request amount if no itemized list is sent
//            invoice.setTotalAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
//        }
//
////        if (status == WorkspaceInvoiceStatus.SENT) {
////            // Default next reminder to 5 days out (simulating schedule)
////            invoice.setNextReminderScheduledAt(request.getIssueDate().plusDays(5));
////        }
//
//        WorkspaceInvoiceEntity saved = invoiceRepository.save(invoice);
//        return mapToResponse(saved);
//    }
//
//    @Transactional(readOnly = true)
//    public InvoiceResponse getInvoice(UUID workspaceId, UUID invoiceId, UUID userId) {
//        WorkspaceInvoiceEntity invoice = invoiceRepository.findByIdFetchCustomer(invoiceId)
//                .orElseThrow(() -> new RuntimeException("Invoice not found"));
//
//        // Check if user & invoice belongs to the workspace
//        if (!userWorkspaceRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
//            throw new RuntimeException("Access denied: Not a member of this workspace");
//        }
//
//        if (!invoice.getWorkspace().getId().equals(workspaceId)) {
//            throw new RuntimeException("Access denied: Invoice does not belong to this workspace");
//        }
//
//        WorkspaceCustomerEntity customer = invoice.getCustomer();
//
//        return InvoiceResponse.builder()
//                .id(invoice.getId())
//                .invoiceNumber(invoice.getInvoiceNumber())
//                .customer(customer)
//                .build();
//    }
//
////    @Transactional
////    public boolean sendReminder(UUID invoiceId) {
////        WorkspaceInvoiceEntity invoice = invoiceRepository.findById(invoiceId)
////                .orElseThrow(() -> new RuntimeException("WorkspaceInvoiceEntity not found"));
////
////        invoice.setLastReminderSentAt(LocalDateTime.now());
////        // Push next reminder back by 5 days
////        if (invoice.getNextReminderScheduledAt() != null) {
////            invoice.setNextReminderScheduledAt(invoice.getNextReminderScheduledAt().plusDays(5));
////        } else {
////            invoice.setNextReminderScheduledAt(invoice.getIssueDate().plusDays(5));
////        }
////        invoiceRepository.save(invoice);
////        return true;
////    }
//
//    private CreateInvoiceResponse mapToResponse(WorkspaceInvoiceEntity invoice) {
//        return CreateInvoiceResponse.builder()
//                .id(invoice.getId())
//                .invoiceNumber(invoice.getInvoiceNumber())
//                .customerId(invoice.getCustomer().getId())
//                .customerName(invoice.getCustomer().getDisplayName())
//                .customerEmail(invoice.getCustomer().getEmail())
//                .amount(invoice.getTotalAmount())
//                .currencyCode(invoice.getCurrency())
//                .status(invoice.getStatus().name())
//                .issueDate(invoice.getIssueDate())
//                .dueDate(invoice.getDueDate())
//                .build();
//    }
//}
