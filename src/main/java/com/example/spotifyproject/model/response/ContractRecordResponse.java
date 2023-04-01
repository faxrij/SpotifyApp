package com.example.spotifyproject.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContractRecordResponse extends CommonResponseField{
    private String name;
    private int monthlyFee;
    private int duration;
    private String remainingDuration;
    private boolean isActive;
    private List<InvoiceResponse> invoices;
}
