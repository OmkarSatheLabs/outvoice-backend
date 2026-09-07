package com.omkarsathe.outvoice.pdf.processor;

import com.omkarsathe.outvoice.pdf.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class PdfProcessorScheduler {

    private final Logger logger = Logger.getLogger(PdfProcessorScheduler.class.getName());
    private final PdfProcessorRepository repository;
    private final PdfProcessorHandlerRegistry handlerRegistry;
    private final PdfService pdfService;

    @Scheduled(fixedDelay = 5000)
    public void processPendingPdfs() {

        List<PdfProcessor> processors =
                repository.findTop20ByStatusOrderByCreatedAtAsc(
                        PdfProcessorStatus.PENDING
                );

        if (processors.isEmpty()) {
            return;
        }

        logger.info("Pending PDF processors found: " + processors.size());

        for (PdfProcessor processor : processors) {
            pdfService.process(processor);
        }
    }

//    private void process(PdfProcessor processor) {
//
//        try {
//
//            processor.setStatus(PdfProcessorStatus.PROCESSING);
//
//            PdfProcessorHandler handler =
//                    handlerRegistry.get(processor.getType());
//
//            handler.process(processor);
//
//            processor.setStatus(PdfProcessorStatus.COMPLETED);
//            processor.setProcessedAt(Instant.now());
//
//        } catch (Exception ex) {
//
//            logger.severe(
//                    "Failed to process PDF processor " +
//                    processor.getId() +
//                    ex
//            );
//
//            processor.setStatus(PdfProcessorStatus.FAILED);
//            processor.setError(ex.getMessage());
//        }
//    }
}
