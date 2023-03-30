package com.example.spotifyproject.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Table(name = "payment")
@Entity
@Getter
@Setter
public class Payment extends Auditable{

    @Column(name = "amount", nullable = false)
    private int amount;

    @ManyToOne
    @JoinColumn(name = "invoice_fk", nullable = false)
    private Invoice invoice_fk;

    @Column(name = "sender_card", nullable = false)
    private String senderCard;

    @Column(name = "receiver_card", nullable = false)
    private String receiverCard;
}
