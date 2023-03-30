package com.example.spotifyproject.model.request.subscription;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@ToString
public class CreateSubscriptionRequest {
    @NotEmpty(message = "name must be entered.")
    private String name;
    @Positive
    private int fee;
    @Positive
    private int duration;
}
