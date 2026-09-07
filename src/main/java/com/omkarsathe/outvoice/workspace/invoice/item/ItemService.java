package com.omkarsathe.outvoice.workspace.invoice.item;

import com.omkarsathe.outvoice.common.exception.ResourceNotFoundException;
import com.omkarsathe.outvoice.workspace.Workspace;
import com.omkarsathe.outvoice.workspace.invoice.Invoice;
import com.omkarsathe.outvoice.workspace.invoice.InvoiceRepository;
import com.omkarsathe.outvoice.workspace.product.Product;
import com.omkarsathe.outvoice.workspace.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final ItemResponseMapper itemResponseMapper;

    public ItemResponse create(Invoice invoice, CreateItem request) {

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + request.productId()
                        ));

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(request.quantity()));

        Item item = Item.builder()
                .invoice(invoice)
                .product(product)
                .quantity(request.quantity())
                .total(total)
                .build();

        invoice.getItems().add(item);

        return itemResponseMapper.toResponse(itemRepository.save(item));
    }
}
