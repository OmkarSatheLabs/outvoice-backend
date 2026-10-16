package com.omkarsathe.outvoice.workspace.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    List<Payment> findByInvoiceId(UUID invoiceId);

    List<Payment> findByWorkspaceId(UUID workspaceId);
}
