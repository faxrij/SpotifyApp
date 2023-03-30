package com.example.spotifyproject.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Table(name = "subscription")
@Entity
@Getter
@Setter
public class Subscription extends Auditable{
    @Column(name = "name")
    private String name;
    @Column(name = "monthly_fee")
    private int monthlyFee;

    @Column(name = "duration")
    private int duration;

    @Column(name = "is_active")
    private boolean isActive;

}
