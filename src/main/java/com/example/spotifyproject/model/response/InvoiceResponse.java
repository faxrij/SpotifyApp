package com.example.spotifyproject.model.response;

import com.example.spotifyproject.entity.ContractRecord;
import com.example.spotifyproject.entity.Payment;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class InvoiceResponse extends CommonResponseField{
    private int fee;
    @JsonIgnoreProperties(value = {"invoice_fk"})
    private List<Payment> payments;
    @JsonIgnoreProperties(value = {"user_fk","invoices"})
    private ContractRecordResponse contract;

}
