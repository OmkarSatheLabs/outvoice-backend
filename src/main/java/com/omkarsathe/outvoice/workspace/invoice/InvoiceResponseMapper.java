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
    @Mapping(target = "customer", expression = "java(mapCustomer(invoice))")
    InvoiceResponse toResponse(Invoice invoice);

    default com.omkarsathe.outvoice.workspace.customer.CustomerResponse mapCustomer(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        com.omkarsathe.outvoice.workspace.customer.Customer c = invoice.getCustomer();
        if (c == null && invoice.getCustomerName() == null) {
            return null;
        }

        String name = invoice.getCustomerName() != null ? invoice.getCustomerName() : (c != null ? c.getFullName() : null);
        String email = invoice.getCustomerEmail() != null ? invoice.getCustomerEmail() : (c != null ? c.getEmail() : null);
        String address = invoice.getCustomerAddress() != null ? invoice.getCustomerAddress() : (c != null ? c.getAddress() : null);
        com.omkarsathe.outvoice.phone.PhoneCodeResponse phoneCode = (c != null && c.getPhoneCode() != null)
                ? new com.omkarsathe.outvoice.phone.PhoneCodeResponse(c.getPhoneCode().getId(), c.getPhoneCode().getCode())
                : null;
        String mobile = c != null ? c.getMobile() : null;
        java.util.UUID id = c != null ? c.getId() : null;

        return new com.omkarsathe.outvoice.workspace.customer.CustomerResponse(
                id,
                name,
                email,
                phoneCode,
                mobile,
                address,
                java.util.List.of(),
                null,
                null,
                null
        );
    }
}
