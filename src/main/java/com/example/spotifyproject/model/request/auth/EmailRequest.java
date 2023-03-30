package com.example.spotifyproject.model.request.auth;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@ToString
public class EmailRequest {

    @Email
    @NotEmpty
    private String email;
}
