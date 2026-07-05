package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.user.UserEntity;
import com.omkarsathe.outvoice.user.UserRepository;
import com.omkarsathe.outvoice.user.workspace.UserWorkspaceRepository;
import com.omkarsathe.outvoice.workspace.WorkspaceEntity;
import com.omkarsathe.outvoice.workspace.WorkspaceRepository;
import com.omkarsathe.outvoice.workspace.customer.CustomerEntity;
import com.omkarsathe.outvoice.workspace.customer.CustomerRepository;
import com.omkarsathe.outvoice.workspace.invoice.dto.CreateInvoiceRequest;
import com.omkarsathe.outvoice.workspace.invoice.dto.InvoiceResponse;
import com.omkarsathe.outvoice.workspace.invoice.dto.InvoiceSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserWorkspaceRepository userWorkspaceRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoices(UUID workspaceId) {
        return invoiceRepository.findByWorkspaceIdFetchCustomer(workspaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvoiceSummaryResponse getInvoiceSummary(UUID workspaceId) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("WorkspaceEntity not found"));

        List<Invoice> invoices = invoiceRepository.findByWorkspaceIdFetchCustomer(workspaceId);

        BigDecimal totalInvoiced = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;

        Map<String, Long> countByStatus = new HashMap<>();
        for (InvoiceStatus status : InvoiceStatus.values()) {
            countByStatus.put(status.name(), 0L);
        }

        for (Invoice inv : invoices) {
            String statusStr = inv.getStatus().name();
            countByStatus.put(statusStr, countByStatus.getOrDefault(statusStr, 0L) + 1);

            BigDecimal amt = inv.getAmount();
            if (inv.getStatus() != InvoiceStatus.DRAFT) {
                totalInvoiced = totalInvoiced.add(amt);
            }
            if (inv.getStatus() == InvoiceStatus.PAID) {
                totalPaid = totalPaid.add(amt);
            } else if (inv.getStatus() == InvoiceStatus.SENT) {
                totalPending = totalPending.add(amt);
            } else if (inv.getStatus() == InvoiceStatus.OVERDUE || inv.getStatus() == InvoiceStatus.DEFAULTED) {
                totalOverdue = totalOverdue.add(amt);
            }
        }

        String currencyCode = workspace.getCurrency() != null ? workspace.getCurrency().getCode() : "INR";

        return InvoiceSummaryResponse.builder()
                .totalInvoiced(totalInvoiced)
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .totalOverdue(totalOverdue)
                .currencyCode(currencyCode)
                .countByStatus(countByStatus)
                .build();
    }

    @Transactional
    public InvoiceResponse createInvoice(UUID workspaceId, UUID createdById, CreateInvoiceRequest request) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("WorkspaceEntity not found"));
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("CustomerEntity not found"));
        UserEntity createdBy = userRepository.getUserById(createdById)
                .orElseThrow(() -> new AuthorizationDeniedException("Not authorized to create invoice"));

        boolean customerIsWorkspaceUser = userWorkspaceRepository
                .existsByWorkspaceIdAndUserId(workspaceId, customer.getId());

        if (customerIsWorkspaceUser) {
            throw new RuntimeException("Cannot create an invoice against a user of this workspace");
        }

        InvoiceStatus status;
        try {
            status = InvoiceStatus.valueOf(request.getStatus().toUpperCase());
        } catch (Exception e) {
            status = InvoiceStatus.DRAFT;
        }

        BigDecimal taxRate = request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO;

        Invoice invoice = Invoice.builder()
                .invoiceNumber(request.getInvoiceNumber())
                .workspace(workspace)
                .customer(customer)
                .currencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode() : "INR")
                .status(status)
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .taxRate(taxRate)
                .createdBy(createdBy)
                .build();

        BigDecimal computedTotal = BigDecimal.ZERO;
        if (request.getLineItems() != null && !request.getLineItems().isEmpty()) {
            for (CreateInvoiceRequest.LineItemDto itemDto : request.getLineItems()) {
                BigDecimal qty = BigDecimal.valueOf(itemDto.getQuantity() != null ? itemDto.getQuantity() : 1);
                BigDecimal lineAmount = qty.multiply(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO);

                InvoiceLineItem item = InvoiceLineItem.builder()
                        .description(itemDto.getDescription())
                        .quantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : 1)
                        .unitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO)
                        .amount(lineAmount)
                        .build();

                invoice.addLineItem(item);
                computedTotal = computedTotal.add(lineAmount);
            }

            // Apply Tax: total = subtotal * (1 + taxRate/100)
            BigDecimal taxMultiplier = BigDecimal.ONE.add(taxRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            computedTotal = computedTotal.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);
            invoice.setAmount(computedTotal);
        } else {
            // Fallback to request amount if no itemized list is sent
            invoice.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
        }

        if (status == InvoiceStatus.SENT) {
            // Default next reminder to 5 days out (simulating schedule)
            invoice.setNextReminderScheduledAt(request.getIssueDate().plusDays(5));
        }

        Invoice saved = invoiceRepository.save(invoice);
        return mapToResponse(saved);
    }

    @Transactional
    public boolean sendReminder(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setLastReminderSentAt(LocalDateTime.now());
        // Push next reminder back by 5 days
        if (invoice.getNextReminderScheduledAt() != null) {
            invoice.setNextReminderScheduledAt(invoice.getNextReminderScheduledAt().plusDays(5));
        } else {
            invoice.setNextReminderScheduledAt(invoice.getIssueDate().plusDays(5));
        }
        invoiceRepository.save(invoice);
        return true;
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerId(invoice.getCustomer().getId())
                .customerName(invoice.getCustomer().getCustomerName())
                .customerEmail(invoice.getCustomer().getEmail())
                .amount(invoice.getAmount())
                .currencyCode(invoice.getCurrencyCode())
                .status(invoice.getStatus().name())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .lastReminderSentAt(invoice.getLastReminderSentAt())
                .nextReminderScheduledAt(invoice.getNextReminderScheduledAt())
                .build();
    }
}
