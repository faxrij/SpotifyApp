package com.example.spotifyproject.model.request.auth;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@ToString
public class LoginRequest {

    @Email
    @NotEmpty
    private String email;
    @NotEmpty
    private String password;
}
