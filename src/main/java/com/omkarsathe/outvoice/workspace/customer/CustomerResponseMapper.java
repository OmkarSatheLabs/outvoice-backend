package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerResponseMapper {

    @Mapping(target = "invoices", source = "invoices")
    @Mapping(target = "totalInvoiced", expression = "java(totalInvoiced(customer.getInvoices()))")
    @Mapping(target = "totalCollected", expression = "java(totalCollected(customer.getInvoices()))")
    @Mapping(target = "outstandingBalance", expression = "java(outstandingBalance(customer.getInvoices()))")
    CustomerResponse toResponse(Customer customer);

    default List<CustomerInvoiceResponse> mapInvoices(List<Invoice> invoices) {
        return invoices.stream()
                .filter(this::isActive)
                .map(this::toInvoiceResponse)
                .toList();
    }

    default BigDecimal totalInvoiced(List<Invoice> invoices) {
        return invoices.stream()
                .filter(this::isActive)
                .map(Invoice::getNetTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal totalCollected(List<Invoice> invoices) {
        return invoices.stream()
                .filter(this::isActive)
                .filter(i -> i.getStatus() == InvoiceStatus.PAID)
                .map(Invoice::getNetTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal outstandingBalance(List<Invoice> invoices) {
        return totalInvoiced(invoices)
                .subtract(totalCollected(invoices));
    }

    default boolean isActive(Invoice invoice) {
        return invoice.getDeletedAt() == null
                && invoice.getStatus() != InvoiceStatus.DRAFT;
    }

    CustomerInvoiceResponse toInvoiceResponse(Invoice invoice);
}
