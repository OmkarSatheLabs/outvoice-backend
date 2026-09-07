package com.omkarsathe.outvoice.pdf.processor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PdfProcessorHandlerRegistry {

    private final Map<PdfType, PdfProcessorHandler> handlers;

    public PdfProcessorHandlerRegistry(
            List<PdfProcessorHandler> handlers
    ) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        PdfProcessorHandler::getType,
                        Function.identity()
                ));
    }

    public PdfProcessorHandler get(PdfType type) {

        PdfProcessorHandler handler = handlers.get(type);

        if (handler == null) {
            throw new IllegalStateException(
                    "No PDF processor handler registered for type: " + type
            );
        }

        return handler;
    }
}
