package com.example.spotifyproject.service.mapper;

import com.example.spotifyproject.entity.Invoice;
import com.example.spotifyproject.model.response.InvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FromInvoiceToInvoiceResponse {
    private final FromContractRecordIntoResponse contractRecordIntoResponse;
    public InvoiceResponse setterFromInvoiceToInvoiceResponse(Invoice invoice) {
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setId(invoice.getId());
        invoiceResponse.setFee(invoice.getFee());
        invoiceResponse.setContract(contractRecordIntoResponse.setterFromContractToContractResponse(invoice.getContract_fk()));
        invoiceResponse.setCreatedDate(invoice.getCreatedDate());
        invoiceResponse.setModifiedDate(invoice.getModifiedDate());

        return invoiceResponse;
    }
}
