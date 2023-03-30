package com.example.spotifyproject.model.response;

import com.example.spotifyproject.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String id;
    private String token;
    private Role role;
    private String name;
    private String lastName;
}
