package com.example.spotifyproject.model.request.User;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@ToString
public class UpdateUserRequest {
    @NotEmpty(message = "name must be entered.")
    private String name;
    @NotEmpty(message = "lastName must be entered.")
    private String lastName;
    @Email(message = "Invalid email")
    @NotEmpty
    private String email;
}
