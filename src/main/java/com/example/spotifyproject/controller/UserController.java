package com.example.spotifyproject.controller;

import com.example.spotifyproject.model.response.InvoiceResponse;
import com.example.spotifyproject.model.response.UserResponse;
import com.example.spotifyproject.service.AuthenticationService;
import com.example.spotifyproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationService authenticationService;


    @GetMapping
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userService.getUsers(pageable, authenticationService.getAuthenticatedUserId());
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable String id) {
        return userService.getUserById(id, authenticationService.getAuthenticatedUserId());
    }

    @GetMapping("/{id}/invoice")
    public Page<InvoiceResponse> getInvoicesByUserId(Pageable pageable,
                                                     @PathVariable String id) {
        return userService.getInvoicesByUserId(pageable, id, authenticationService.getAuthenticatedUserId());
    }

    @PostMapping("/{userId}/subscribe/{subId}")
    public void subscribe(@PathVariable String userId,
                          @PathVariable String subId) {
        userService.subscribe(userId, subId, authenticationService.getAuthenticatedUserId());
    }


    // TO BE MADE

//    @PutMapping("/{id}")
//    public void updateUserById(@PathVariable String id, UpdateUserRequest updateUserRequest) {
//        userService.updateUserById(id, updateUserRequest, authenticationService.getAuthenticatedUserId());
//    }
}
