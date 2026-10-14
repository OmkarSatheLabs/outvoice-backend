package com.omkarsathe.outvoice.workspace.invoice;

import com.omkarsathe.outvoice.workspace.WorkspaceResponseMapper;
import com.omkarsathe.outvoice.workspace.customer.CustomerResponseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {
                CustomerResponseMapper.class,
                WorkspaceResponseMapper.class,
        }
)
public interface InvoiceResponseMapper {

    @Mapping(target = "pdfUrl", expression = "java(invoice.getPdfStatus() == com.omkarsathe.outvoice.pdf.processor.PdfProcessorStatus.COMPLETED ? \"/api/workspaces/\" + invoice.getWorkspace().getId() + \"/invoices/\" + invoice.getId() + \"/pdf\" : null)")
    InvoiceResponse toResponse(Invoice invoice);
}
