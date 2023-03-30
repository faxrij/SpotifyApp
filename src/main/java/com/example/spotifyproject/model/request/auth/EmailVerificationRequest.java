package com.example.spotifyproject.model.request.auth;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@ToString
public class EmailVerificationRequest {
    @Email
    @NotEmpty(message = "Email must be filled.")
    private String email;

    @NotEmpty(message = "Code must be filled.")
    private String verificationCode;
}
