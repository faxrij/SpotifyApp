package com.example.spotifyproject.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Table(name = "invoice")
@Entity
@Getter
@Setter
public class Invoice extends Auditable {

    @Column(name = "fee",nullable = false)
    private int fee;

    @ManyToOne
    @JoinColumn(name = "contract_fk", nullable = false)
    private ContractRecord contract_fk;

}
