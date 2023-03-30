package com.example.spotifyproject.model.request.subscription;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@ToString
public class UpdateSubscriptionRequest {
    @NotEmpty
    private String name;
    @NotNull
    @Positive
    private int fee;
    @NotNull
    @Positive
    private int duration;
    @NotNull
    private Boolean isActive;
}
