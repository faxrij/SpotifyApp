package com.example.spotifyproject.service.mapper;

import com.example.spotifyproject.entity.Subscription;
import com.example.spotifyproject.model.response.SubscriptionResponse;
import org.springframework.stereotype.Service;

@Service
public class FromSubscriptionToSubscriptionResponse {

    public SubscriptionResponse fromSubscriptionToSubscriptionResponse(Subscription subscription) {
        SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
        subscriptionResponse.setId(subscription.getId());
        subscriptionResponse.setActive(subscription.isActive());
        subscriptionResponse.setName(subscription.getName());
        subscriptionResponse.setDuration(subscription.getDuration());
        subscriptionResponse.setFee(subscription.getMonthlyFee());
        subscriptionResponse.setCreatedDate(subscription.getCreatedDate());
        subscriptionResponse.setModifiedDate(subscription.getModifiedDate());
        return subscriptionResponse;
    }
}
