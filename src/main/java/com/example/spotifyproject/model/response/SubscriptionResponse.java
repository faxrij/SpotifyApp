package com.example.spotifyproject.model.response;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class SubscriptionResponse extends CommonResponseField {
    private String name;
    private int fee;
    private int duration;
    private boolean isActive;

}
