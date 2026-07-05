package com.omkarsathe.outvoice.workspace.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, UUID> {
}
