package com.omkarsathe.outvoice.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.omkarsathe.outvoice.common.exception.InvoicePdfGenerationException;
import com.omkarsathe.outvoice.mail.MailService;
import com.omkarsathe.outvoice.pdf.processor.PdfProcessor;
import com.omkarsathe.outvoice.pdf.processor.PdfProcessorRepository;
import com.omkarsathe.outvoice.pdf.processor.PdfProcessorStatus;
import com.omkarsathe.outvoice.pdf.processor.PdfType;
import com.omkarsathe.outvoice.sms.SmsService;
import com.omkarsathe.outvoice.storage.ObjectStorageService;
import com.omkarsathe.outvoice.storage.model.StorageMetadata;
import com.omkarsathe.outvoice.storage.model.StorageObject;
import com.omkarsathe.outvoice.storage.model.UploadRequest;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.customer.Customer;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceAccessToken;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceAccessTokenService;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfServiceStorageTest {

    @Mock
    private PdfProcessorRepository pdfProcessorRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private MailService mailService;
    @Mock
    private SmsService smsService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private InvoiceAccessTokenService tokenService;
    @Mock
    private ObjectStorageService objectStorageService;

    @Captor
    private ArgumentCaptor<UploadRequest> uploadCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PdfService pdfService;

    private UUID workspaceId;
    private UUID invoiceId;
    private UUID processorId;
    private Invoice invoice;
    private PdfProcessor processor;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService(
                pdfProcessorRepository,
                objectMapper,
                invoiceRepository,
                mailService,
                smsService,
                eventPublisher,
                tokenService,
                objectStorageService
        );

        workspaceId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        processorId = UUID.randomUUID();

        Workspace workspace = Workspace.builder()
                .id(workspaceId)
                .name("Acme Corp")
                .build();

        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .workspace(workspace)
                .fullName("John Doe")
                .email("john@example.com")
                .build();

        invoice = Invoice.builder()
                .id(invoiceId)
                .workspace(workspace)
                .customer(customer)
                .invoiceNumber("INV-00100")
                .total(new BigDecimal("1000.00"))
                .tax(new BigDecimal("180.00"))
                .discount(BigDecimal.ZERO)
                .netTotal(new BigDecimal("1180.00"))
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .items(new ArrayList<>())
                .build();

        ObjectNode data = objectMapper.createObjectNode();
        data.put("invoiceId", invoiceId.toString());

        processor = PdfProcessor.builder()
                .id(processorId)
                .type(PdfType.INVOICE)
                .data(data)
                .status(PdfProcessorStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void process_uploadsPdfToObjectStorage_andUpdatesProcessorAndInvoice() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(tokenService.getOrCreateToken(any())).thenReturn(
                InvoiceAccessToken.builder().tokenHash("dummy-hash").build()
        );
        when(tokenService.buildPublicInvoiceUrl("dummy-hash")).thenReturn("https://outvoice.com/invoices/dummy-hash");

        pdfService.process(processor);

        // Verify object storage upload
        verify(objectStorageService).upload(uploadCaptor.capture());
        UploadRequest capturedRequest = uploadCaptor.getValue();

        String expectedKey = "workspaces/" + workspaceId + "/invoices/" + invoiceId + "/" + processorId + ".pdf";
        assertEquals(expectedKey, capturedRequest.objectKey());
        assertEquals("application/pdf", capturedRequest.contentType());
        assertTrue(capturedRequest.contentLength() > 0);
        assertEquals(invoiceId.toString(), capturedRequest.userMetadata().get("invoiceId"));
        assertEquals(workspaceId.toString(), capturedRequest.userMetadata().get("workspaceId"));
        assertEquals("INV-00100", capturedRequest.userMetadata().get("invoiceNumber"));
        assertTrue(capturedRequest.isPublic());

        // Verify processor saved with COMPLETED and objectKey
        verify(pdfProcessorRepository).save(processor);
        assertEquals(PdfProcessorStatus.COMPLETED, processor.getStatus());
        assertEquals(expectedKey, processor.getLocation());
        assertNotNull(processor.getProcessedAt());
        assertNull(processor.getError());

        // Verify invoice saved with COMPLETED and objectKey
        verify(invoiceRepository).save(invoice);
        assertEquals(PdfProcessorStatus.COMPLETED, invoice.getPdfStatus());
        assertEquals(expectedKey, invoice.getPdfLocation());
    }

    @Test
    void process_cleansUpOldStorageObject_whenReplaced() {
        String oldKey = "workspaces/" + workspaceId + "/invoices/" + invoiceId + "/old-processor.pdf";
        invoice.setPdfLocation(oldKey);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(tokenService.getOrCreateToken(any())).thenReturn(
                InvoiceAccessToken.builder().tokenHash("hash").build()
        );
        when(tokenService.buildPublicInvoiceUrl("hash")).thenReturn("https://outvoice.com/invoices/hash");

        pdfService.process(processor);

        verify(objectStorageService).delete(oldKey);
    }

    @Test
    void process_compensatesByDeletingUploadedObject_whenSaveFails() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(pdfProcessorRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        assertThrows(InvoicePdfGenerationException.class, () -> pdfService.process(processor));

        String expectedKey = "workspaces/" + workspaceId + "/invoices/" + invoiceId + "/" + processorId + ".pdf";
        verify(objectStorageService).delete(expectedKey);
    }

    @Test
    void getInvoicePdfBytes_loadsFromObjectStorage_whenObjectExists() throws Exception {
        String objectKey = "workspaces/" + workspaceId + "/invoices/" + invoiceId + "/invoice.pdf";
        invoice.setPdfLocation(objectKey);

        byte[] expectedBytes = "PDF content from storage".getBytes();
        StorageMetadata metadata = new StorageMetadata(
                objectKey,
                expectedBytes.length,
                "application/pdf",
                Instant.now(),
                null,
                Collections.emptyMap()
        );
        StorageObject storageObject = new StorageObject(
                objectKey,
                new ByteArrayInputStream(expectedBytes),
                metadata
        );

        when(objectStorageService.exists(objectKey)).thenReturn(true);
        when(objectStorageService.load(objectKey)).thenReturn(storageObject);

        byte[] actualBytes = pdfService.getInvoicePdfBytes(invoice);

        assertArrayEquals(expectedBytes, actualBytes);
        verify(objectStorageService).load(objectKey);
    }

    @Test
    void getInvoicePdfBytes_fallsBackToGeneratePdf_whenObjectDoesNotExist() throws Exception {
        String objectKey = "workspaces/" + workspaceId + "/invoices/" + invoiceId + "/missing.pdf";
        invoice.setPdfLocation(objectKey);

        when(objectStorageService.exists(objectKey)).thenReturn(false);

        byte[] actualBytes = pdfService.getInvoicePdfBytes(invoice);

        assertNotNull(actualBytes);
        assertTrue(actualBytes.length > 0);
        verify(objectStorageService, never()).load(objectKey);
    }

    @Test
    void loadPdf_and_getPdfUrl_delegateToObjectStorageService() {
        String objectKey = "workspaces/" + workspaceId + "/invoices/" + invoiceId + "/test.pdf";

        when(objectStorageService.getUrl(objectKey)).thenReturn("http://localhost:8080/files/" + objectKey);

        assertEquals("http://localhost:8080/files/" + objectKey, pdfService.getPdfUrl(objectKey));
        verify(objectStorageService).getUrl(objectKey);

        StorageObject mockStorageObject = mock(StorageObject.class);
        when(objectStorageService.load(objectKey)).thenReturn(mockStorageObject);

        assertEquals(mockStorageObject, pdfService.loadPdf(objectKey));
        verify(objectStorageService).load(objectKey);
    }
}
