package com.example.spotifyproject.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class PayInvoiceRequest {
    @NotEmpty
    @Length(min = 8, message = "Sender Card should be at least 8 characters long")
    private String senderCard;

    @NotNull
    private Integer amount;

    @NotEmpty
    @Length(min = 8, message = "Receiver Card should be at least 8 characters long")
    private String receiverCard;
}
