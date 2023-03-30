package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.Role;
import com.example.spotifyproject.entity.Subscription;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.model.request.subscription.CreateSubscriptionRequest;
import com.example.spotifyproject.model.request.subscription.UpdateSubscriptionRequest;
import com.example.spotifyproject.model.response.SubscriptionResponse;
import com.example.spotifyproject.repository.SubscriptionRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromSubscriptionToSubscriptionResponse;
import com.example.spotifyproject.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FromSubscriptionToSubscriptionResponse fromSubscriptionToSubscriptionResponse;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void testGetSubscriptions() {
        // given
        Subscription subscription1 = new Subscription();
        subscription1.setId("1");
        subscription1.setDuration(2);
        subscription1.setMonthlyFee(100);
        subscription1.setActive(false);
        subscription1.setName("SUB1");
        subscription1.setCreatedDate(DateUtil.now());
        subscription1.setModifiedDate(DateUtil.now());

        SubscriptionResponse subscriptionResponse = new SubscriptionResponse();

        subscriptionResponse.setFee(subscription1.getMonthlyFee());
        subscriptionResponse.setName(subscription1.getName());
        subscriptionResponse.setDuration(subscription1.getDuration());
        subscriptionResponse.setActive(subscription1.isActive());
        subscriptionResponse.setId(subscription1.getId());
        subscriptionResponse.setModifiedDate(subscription1.getModifiedDate());
        subscriptionResponse.setCreatedDate(subscription1.getCreatedDate());

        List<Subscription> subscriptions = Collections.singletonList(subscription1);

        Page<Subscription> subscriptionPage = new PageImpl<>(subscriptions);

        // when
        when(subscriptionRepository.findAll(Pageable.unpaged())).thenReturn(subscriptionPage);
        when(fromSubscriptionToSubscriptionResponse.fromSubscriptionToSubscriptionResponse(any(Subscription.class))).thenReturn(subscriptionResponse);

        Page<SubscriptionResponse> responsePage = subscriptionService.getSubscriptions(Pageable.unpaged());

        // then
        assertEquals(1, responsePage.getTotalElements());
        assertFalse(responsePage.getContent().isEmpty());
        assertEquals(responsePage.getContent().get(0).getName(),subscription1.getName());
        assertEquals(responsePage.getContent().get(0).getFee(),subscription1.getMonthlyFee());
        assertEquals(responsePage.getContent().get(0).getId(),subscription1.getId());
        assertEquals(responsePage.getContent().get(0).getDuration(),subscription1.getDuration());
        assertEquals(responsePage.getContent().get(0).getCreatedDate(),subscription1.getCreatedDate());
        assertEquals(responsePage.getContent().get(0).getModifiedDate(),subscription1.getModifiedDate());
    }

    @Test
    void testGetSubscriptionById() {
        // given
        Subscription subscription = new Subscription();
        subscription.setId("1");
        subscription.setName("PERFECT");
        subscription.setDuration(6);
        subscription.setModifiedDate(DateUtil.now());
        subscription.setCreatedDate(DateUtil.now());
        subscription.setMonthlyFee(20);
        subscription.setActive(true);

        SubscriptionResponse expected = new SubscriptionResponse();
        expected.setId("1");
        expected.setName("PERFECT");
        expected.setDuration(6);
        expected.setModifiedDate(DateUtil.now());
        expected.setCreatedDate(DateUtil.now());
        expected.setFee(20);

        // when

        when(subscriptionRepository.findById("1")).thenReturn(Optional.of(subscription));
        when(fromSubscriptionToSubscriptionResponse.fromSubscriptionToSubscriptionResponse(Mockito.any())).thenReturn(expected);

        SubscriptionResponse response = subscriptionService.getSubscriptionsById("1");

        // then
        assertEquals(subscription.getName(), response.getName());
        assertEquals(subscription.getDuration(), response.getDuration());
        assertEquals(subscription.getId(), response.getId());
    }

    @Test
    void testGetSubscriptionByIdNotFound() {
        // given
        when(subscriptionRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());

        // when + then
        assertThrows(BusinessException.class, () -> subscriptionService.getSubscriptionsById("1"));
    }

    @Test
    void testAddSubscription() {
        // given
        String userId = "1";
        User adminUser = new User();
        adminUser.setId("1");
        adminUser.setEmail("user@example.com");
        adminUser.setPasswordHash("password");
        adminUser.setRole(Role.ADMIN);
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setName("Test Subscription");
        request.setDuration(30);
        request.setFee(10);
        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // when
        subscriptionService.addSubscription(request, userId);

        // then
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testAddSubscriptionNonAdminUser() {
        // given
        String userId = "1";
        User regularUser = new User();
        regularUser.setId("1");
        regularUser.setEmail("user@example.com");
        regularUser.setPasswordHash("password");
        regularUser.setRole(Role.MEMBER);
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setName("Test Subscription");
        request.setFee(10);
        request.setDuration(30);
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));

        // when + then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> subscriptionService.addSubscription(request, userId));
        assertEquals("unauthorized", exception.getErrorCode());
    }

    @Test
    void testAddSubscriptionNonExistentUser() {
        // given
        String userId = "1";
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setName("Test Subscription");
        request.setDuration(30);
        request.setFee(10);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when + then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> subscriptionService.addSubscription(request, userId));
        assertEquals("account_missing", exception.getErrorCode());
    }

    @Test
    void testUpdateSubscriptionSuccess() {
        // given
        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setName("new name");
        request.setDuration(30);
        request.setFee(10);
        request.setIsActive(true);
        String id = "123";
        String userId = "456";

        User user = new User();
        user.setRole(Role.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setName("old name");
        subscription.setDuration(60);
        subscription.setMonthlyFee(20);
        subscription.setActive(false);
        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));

        // when
        subscriptionService.updateSubscription(request, id, userId);

        // then
        ArgumentCaptor<Subscription> argumentCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(argumentCaptor.capture());
        Subscription savedSubscription = argumentCaptor.getValue();

        assertEquals(request.getName(), savedSubscription.getName());
        assertEquals(request.getDuration(), savedSubscription.getDuration());
        assertEquals(request.getFee(), savedSubscription.getMonthlyFee());
        assertEquals(request.getIsActive(), savedSubscription.isActive());
        assertNotNull(savedSubscription.getModifiedDate());
    }


    @Test
    void testUpdateSubscriptionUnauthorized() {
        // given
        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        String id = "123";
        String userId = "456";

        User user = new User();
        user.setRole(Role.MEMBER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when + then
        assertThrows(BusinessException.class, () -> subscriptionService.updateSubscription(request, id, userId));
        verify(userRepository).findById(userId);
        verify(subscriptionRepository, never()).findById(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void testUpdateSubscriptionSubscriptionNotFound() {
        // given
        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        String id = "123";
        String userId = "456";

        User user = new User();
        user.setRole(Role.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

        // when + then
        assertThrows(BusinessException.class, () -> subscriptionService.updateSubscription(request, id, userId));
        verify(userRepository).findById(userId);
        verify(subscriptionRepository).findById(id);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void testUpdateSubscriptionUserNotFound() {
        //given

        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        String id = "123";
        String userId = "456";

        //when + then
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> subscriptionService.updateSubscription(request, id, userId));

    }

    @Test
    void testDeleteSubscriptionUserNotFound() {
        //given

        String id = "456";
        String userId = "123";
        User user = new User();
        user.setRole(Role.ADMIN);

        //when + then
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> subscriptionService.deleteSubscription(id, userId));

    }

    @Test
    void testDeleteSubscriptionUserNotAuthenticated() {
        //given

        String id = "456";
        String userId = "123";
        User user = new User();
        user.setRole(Role.MEMBER);

        //when + then
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> subscriptionService.deleteSubscription(id, userId));

    }

    @Test
    void testDeleteSubscriptionSubscriptionDoesNotExist() {
        String id = "456";
        String userId = "123";
        User user = new User();
        user.setRole(Role.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> subscriptionService.deleteSubscription(id, userId));

    }

    @Test
    void testDeleteSubscriptionSuccess() {
        String id = "456";
        String userId = "123";
        Subscription subscription = new Subscription();
        subscription.setId(id);

        User user = new User();
        user.setRole(Role.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));

        // when
        subscriptionService.deleteSubscription(id, userId);

        // then
        ArgumentCaptor<Subscription> argumentCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(argumentCaptor.capture());
        Subscription savedSubscription = argumentCaptor.getValue();

        assertFalse(savedSubscription.isActive());
        verify(subscriptionRepository).save(savedSubscription);
    }
}