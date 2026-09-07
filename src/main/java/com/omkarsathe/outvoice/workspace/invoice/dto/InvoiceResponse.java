//package com.omkarsathe.outvoice.workspace.invoice.dto;
//
//import com.omkarsathe.outvoice.user.UserEntity;
//import com.omkarsathe.outvoice.workspace.customer.WorkspaceCustomerEntity;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class InvoiceResponse {
//    private UUID id;
//    private String invoiceNumber;
//    private WorkspaceCustomerEntity customer;
//    private BigDecimal amount;
//    private String currencyCode;
//    private String status;
//    private LocalDate issueDate;
//    private LocalDate dueDate;
//    private LocalDateTime lastReminderSentAt;
//    private LocalDate nextReminderScheduledAt;
//    private BigDecimal taxRate;
//}
