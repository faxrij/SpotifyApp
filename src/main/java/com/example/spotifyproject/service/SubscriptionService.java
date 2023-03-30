package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.Role;
import com.example.spotifyproject.entity.Subscription;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.exception.ErrorCode;
import com.example.spotifyproject.model.request.subscription.CreateSubscriptionRequest;
import com.example.spotifyproject.model.request.subscription.UpdateSubscriptionRequest;
import com.example.spotifyproject.model.response.SubscriptionResponse;
import com.example.spotifyproject.repository.SubscriptionRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromSubscriptionToSubscriptionResponse;
import com.example.spotifyproject.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final FromSubscriptionToSubscriptionResponse fromSubscriptionToSubscriptionResponse;

    public Page<SubscriptionResponse> getSubscriptions(Pageable pageable) {

        Page<Subscription> subscriptions = subscriptionRepository.findAll(pageable);
        return subscriptions.map(fromSubscriptionToSubscriptionResponse::fromSubscriptionToSubscriptionResponse);
    }

    public SubscriptionResponse getSubscriptionsById(String id) {
          Subscription response = subscriptionRepository.findById(id).orElseThrow(
                  () -> new BusinessException(ErrorCode.account_missing, "There is no subscription with given id"));

          return fromSubscriptionToSubscriptionResponse.fromSubscriptionToSubscriptionResponse(response);
    }

    public void addSubscription(CreateSubscriptionRequest body, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User does not exist"));

        if (!user.getRole().equals(Role.ADMIN)){
            throw new BusinessException(ErrorCode.unauthorized, "User is not authorized");
        }

        Subscription newSubscription = new Subscription();
        newSubscription.setName(body.getName());
        newSubscription.setMonthlyFee(body.getFee());
        newSubscription.setDuration(body.getDuration());
        newSubscription.setCreatedDate(DateUtil.now());
        newSubscription.setModifiedDate(DateUtil.now());
        newSubscription.setActive(true);

        subscriptionRepository.save(newSubscription);
    }

    public void updateSubscription(UpdateSubscriptionRequest body, String id, String userId) {
        Subscription subscription = checker(id, userId);

        subscription.setName(body.getName());
        subscription.setActive(body.getIsActive());
        subscription.setDuration(body.getDuration());
        subscription.setModifiedDate(DateUtil.now());
        subscription.setMonthlyFee(body.getFee());
        subscriptionRepository.save(subscription);
    }

    private Subscription checker(String id, String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "User does not exist")
        );

        if(!user.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.unauthorized, "User is not authenticated");
        }

        return subscriptionRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.resource_missing, "Subscription with provided id does not exist")
        );
    }

    public void deleteSubscription(String id, String userId) {
        Subscription subscription = checker(id, userId);

        subscription.setActive(false);
        subscription.setModifiedDate(DateUtil.now());
        subscriptionRepository.save(subscription);

    }
}
