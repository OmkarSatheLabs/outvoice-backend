package com.omkarsathe.outvoice.pdf;

import com.omkarsathe.outvoice.pdf.processor.PdfProcessor;
import com.omkarsathe.outvoice.pdf.processor.PdfProcessorRepository;
import com.omkarsathe.outvoice.pdf.processor.PdfProcessorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class InvoicePdfEventListener {

    private final Logger logger = Logger.getLogger(InvoicePdfEventListener.class.getName());
    private final PdfProcessorRepository pdfProcessorRepository;
    private final PdfService pdfService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleInvoicePdfGenerate(InvoicePdfGenerateEvent event) {
        logger.info("Handling event-driven PDF generation for processor id: " + event.processorId());
        pdfProcessorRepository.findById(event.processorId()).ifPresent(processor -> {
            if (processor.getStatus() == PdfProcessorStatus.PENDING) {
                try {
                    pdfService.process(processor);
                } catch (Exception ex) {
                    logger.severe("Error in async PDF processing for processor id " + event.processorId() + ": " + ex.getMessage());
                }
            }
        });
    }
}
