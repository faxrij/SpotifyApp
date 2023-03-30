package com.example.spotifyproject.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Table(name = "contract_record")
@Entity
@Getter
@Setter
public class ContractRecord extends Auditable{
    @Column(name = "name")
    private String name;
    @Column(name = "monthly_fee")
    private int monthlyFee;

    @Column(name = "duration")
    private int duration;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(targetEntity = Invoice.class, cascade = CascadeType.ALL, mappedBy = "contract_fk")
    private List<Invoice> invoices;

    @ManyToOne
    @JoinColumn(name = "user_fk")
    private User user_fk;
}
