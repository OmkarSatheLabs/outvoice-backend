package com.omkarsathe.outvoice.workspace.invoice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(
                invoiceRepository,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void getSummary_EmptyList_ReturnsAllZeros() {
        UUID workspaceId = UUID.randomUUID();
        when(invoiceRepository.findByWorkspace_IdAndDeletedAtIsNull(workspaceId))
                .thenReturn(List.of());

        InvoiceSummaryResponse summary = invoiceService.getSummary(workspaceId);

        assertEquals(BigDecimal.ZERO, summary.totalInvoiced());
        assertEquals(BigDecimal.ZERO, summary.totalPaid());
        assertEquals(BigDecimal.ZERO, summary.totalPending());
        assertEquals(BigDecimal.ZERO, summary.totalOverdue());
    }

    @Test
    void getSummary_CorrectlyCalculatesTotalsByStatus() {
        UUID workspaceId = UUID.randomUUID();

        Invoice draft = Invoice.builder()
                .status(InvoiceStatus.DRAFT)
                .netTotal(new BigDecimal("100.00"))
                .build();

        Invoice cancelled = Invoice.builder()
                .status(InvoiceStatus.CANCELLED)
                .netTotal(new BigDecimal("200.00"))
                .build();

        Invoice deleted = Invoice.builder()
                .status(InvoiceStatus.PAID)
                .netTotal(new BigDecimal("300.00"))
                .deletedAt(Instant.now())
                .build();

        Invoice paid = Invoice.builder()
                .status(InvoiceStatus.PAID)
                .netTotal(new BigDecimal("1000.00"))
                .build();

        Invoice overdue = Invoice.builder()
                .status(InvoiceStatus.OVERDUE)
                .netTotal(new BigDecimal("400.00"))
                .build();

        Invoice defaulted = Invoice.builder()
                .status(InvoiceStatus.DEFAULTED)
                .netTotal(new BigDecimal("250.00"))
                .build();

        Invoice sent = Invoice.builder()
                .status(InvoiceStatus.SENT)
                .netTotal(new BigDecimal("500.00"))
                .build();

        Invoice created = Invoice.builder()
                .status(InvoiceStatus.CREATED)
                .netTotal(new BigDecimal("150.00"))
                .build();

        Invoice viewed = Invoice.builder()
                .status(InvoiceStatus.VIEWED)
                .netTotal(new BigDecimal("350.00"))
                .build();

        Invoice acknowledged = Invoice.builder()
                .status(InvoiceStatus.ACKNOWLEDGED)
                .netTotal(new BigDecimal("50.00"))
                .build();

        Invoice nullNetTotal = Invoice.builder()
                .status(InvoiceStatus.SENT)
                .netTotal(null)
                .build();

        when(invoiceRepository.findByWorkspace_IdAndDeletedAtIsNull(workspaceId))
                .thenReturn(List.of(
                        draft,
                        cancelled,
                        deleted,
                        paid,
                        overdue,
                        defaulted,
                        sent,
                        created,
                        viewed,
                        acknowledged,
                        nullNetTotal
                ));

        InvoiceSummaryResponse summary = invoiceService.getSummary(workspaceId);

        // Paid: 1000.00
        assertEquals(new BigDecimal("1000.00"), summary.totalPaid());

        // Overdue: 400.00 + 250.00 = 650.00
        assertEquals(new BigDecimal("650.00"), summary.totalOverdue());

        // Pending: 500.00 + 150.00 + 350.00 + 50.00 + 0 = 1050.00
        assertEquals(new BigDecimal("1050.00"), summary.totalPending());

        // Total invoiced: 1000.00 + 650.00 + 1050.00 = 2700.00
        assertEquals(new BigDecimal("2700.00"), summary.totalInvoiced());

        // Total Invoiced matches sum of Paid + Pending + Overdue
        assertEquals(
                summary.totalInvoiced(),
                summary.totalPaid().add(summary.totalPending()).add(summary.totalOverdue())
        );
    }
}
