package com.omkarsathe.outvoice.pdf.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omkarsathe.outvoice.pdf.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class InvoicePdfProcessorHandler implements PdfProcessorHandler {

    private final ObjectMapper objectMapper;
    private final PdfService pdfService;
    private final PdfProcessorRepository pdfProcessorRepository;

    @Override
    public PdfType getType() {
        return PdfType.INVOICE;
    }

    @Override
    public void queue(PdfType type, InvoicePdfProcessorData data) {
        PdfProcessor processor = PdfProcessor.builder()
                .type(type)
                .data(objectMapper.valueToTree(data))
                .status(PdfProcessorStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        pdfProcessorRepository.save(processor);
    }
}
