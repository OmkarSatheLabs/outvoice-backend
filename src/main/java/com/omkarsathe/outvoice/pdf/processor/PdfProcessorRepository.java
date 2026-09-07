package com.omkarsathe.outvoice.pdf.processor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PdfProcessorRepository extends JpaRepository<PdfProcessor, UUID> {

    List<PdfProcessor> findTop20ByStatusOrderByCreatedAtAsc(
            PdfProcessorStatus status
    );
}
