package com.example.spotifyproject.controller;

import com.example.spotifyproject.model.request.subscription.CreateSubscriptionRequest;
import com.example.spotifyproject.model.request.subscription.UpdateSubscriptionRequest;
import com.example.spotifyproject.model.response.SubscriptionResponse;
import com.example.spotifyproject.service.AuthenticationService;
import com.example.spotifyproject.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public Page<SubscriptionResponse> getSubscriptions(Pageable pageable) {
        return subscriptionService.getSubscriptions(pageable);
    }

    @GetMapping("/{id}")
    public SubscriptionResponse getSubscriptionsById(@PathVariable String id) {
        return subscriptionService.getSubscriptionsById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addSubscription(@Valid @RequestBody CreateSubscriptionRequest createSubscriptionRequest) {
        subscriptionService.addSubscription(createSubscriptionRequest, authenticationService.getAuthenticatedUserId());
    }

    @PutMapping("/{id}")
    public void updateSubscription(@Valid @RequestBody UpdateSubscriptionRequest updateSubscriptionRequest,
                                   @PathVariable String id) {
        subscriptionService.updateSubscription(updateSubscriptionRequest, id,authenticationService.getAuthenticatedUserId());
    }

    @DeleteMapping("/{id}")
    public void deleteSubscription(@PathVariable String id) {
        subscriptionService.deleteSubscription(id, authenticationService.getAuthenticatedUserId());
    }
}
