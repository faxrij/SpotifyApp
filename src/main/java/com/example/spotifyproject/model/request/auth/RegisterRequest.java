package com.example.spotifyproject.model.request.auth;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@ToString
public class RegisterRequest {
    @NotEmpty(message = "name must be entered.")
    private String name;
    @NotEmpty(message = "lastName must be entered.")
    private String lastName;
    @Email(message = "Invalid email")
    @NotEmpty
    private String email;
    @NotEmpty(message = "password must be entered.")
    @Length(min = 8, max = 10)
    private String password;


}
