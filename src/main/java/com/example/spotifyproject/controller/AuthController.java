package com.example.spotifyproject.controller;

import com.example.spotifyproject.model.request.auth.*;
import com.example.spotifyproject.model.response.LoginResponse;
import com.example.spotifyproject.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authenticationService.login(loginRequest);
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest registerRequest) {
        authenticationService.register(registerRequest);
    }

    @PostMapping("/verify")
    public void verify(@Valid @RequestBody EmailVerificationRequest emailVerificationRequest) {
        authenticationService.verify(emailVerificationRequest);
    }

    @PostMapping("/verify/email")
    public void sendVerificationEmail(@Valid @RequestBody EmailRequest emailRequest) {
       authenticationService.verifyEmail(emailRequest);
    }

    @PostMapping("/recover")
    public void recover(@Valid @RequestBody EmailRecoveryRequest emailRecoveryRequest) {
        authenticationService.recover(emailRecoveryRequest);
    }

    @PostMapping("/recover/email")
    public void sendRecoveryEmail(@Valid @RequestBody EmailRequest emailRequest) {
        authenticationService.sendRecoveryEmail(emailRequest);
    }
}
