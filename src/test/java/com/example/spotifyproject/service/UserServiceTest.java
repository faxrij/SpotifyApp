package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.*;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.model.response.InvoiceResponse;
import com.example.spotifyproject.model.response.UserResponse;
import com.example.spotifyproject.repository.ContractRepository;
import com.example.spotifyproject.repository.InvoiceRepository;
import com.example.spotifyproject.repository.SubscriptionRepository;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.service.mapper.FromInvoiceToInvoiceResponse;
import com.example.spotifyproject.service.mapper.FromUserToUserResponse;
import com.example.spotifyproject.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private FromUserToUserResponse fromUserToUserResponse;

    @InjectMocks
    private UserService userService;

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private FromInvoiceToInvoiceResponse fromInvoiceToInvoiceResponse;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Captor
    private ArgumentCaptor<ContractRecord> captor;


    @Test
    public void testGetUsers() {
        // given
        User adminUser = new User();
        adminUser.setId("123");
        adminUser.setRole(Role.ADMIN);
        when(userRepository.findById("123")).thenReturn(Optional.of(adminUser));

        Pageable pageable = PageRequest.of(0, 10);
        List<User> userList = new ArrayList<>();
        userList.add(new User());
        userList.add(new User());
        Page<User> pageOfUsers = new PageImpl<>(userList, pageable, 2);
        when(userRepository.findAll(pageable)).thenReturn(pageOfUsers);

        UserResponse userResponse1 = new UserResponse();
        UserResponse userResponse2 = new UserResponse();
        when(fromUserToUserResponse.fromUserToUserResponse(Mockito.any())).thenReturn(userResponse1, userResponse2);

        Page<UserResponse> result = userService.getUsers(pageable, "123");

        assertEquals(2, result.getContent().size());
        verify(userRepository).findById("123");
        verify(userRepository).findAll(pageable);
        verify(fromUserToUserResponse, times(2)).fromUserToUserResponse(Mockito.any());
    }

    @Test
    public void testGetAllUsersAsNonAdmin() {
        // given
        User nonAdminUser = new User();
        nonAdminUser.setRole(Role.MEMBER);
        when(userRepository.findById("123")).thenReturn(Optional.of(nonAdminUser));
        assertThrows(BusinessException.class, () ->  userService.getUsers(PageRequest.of(0, 10), "123"));
    }
    @Test
    public void testGetAllUsersAsNonExistingUser() {
        // given
        User nonAdminUser = new User();
        nonAdminUser.setRole(Role.MEMBER);
        assertThrows(BusinessException.class, () ->  userService.getUsers(PageRequest.of(0, 10), anyString()));
    }
    @Test
    public void testGetUserById() {
        // Arrange
        User adminUser = new User();
        adminUser.setId("123");
        adminUser.setRole(Role.ADMIN);


        User testUser = new User();
        testUser.setId("456");
        testUser.setEmail("test@example.com");
        testUser.setModifiedDate(DateUtil.now());
        testUser.setCreatedDate(DateUtil.now());

        testUser.setRole(Role.MEMBER);

        UserResponse userResponse = new UserResponse();
        userResponse.setRole(testUser.getRole());
        userResponse.setEmail(testUser.getEmail());
        userResponse.setName(testUser.getName());
        userResponse.setId(testUser.getId());

        when(userRepository.findById("456")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("123")).thenReturn(Optional.of(adminUser));

        when(fromUserToUserResponse.fromUserToUserResponse(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getUserById("456", "123");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        Mockito.verify(userRepository).findById("456");
        verify(fromUserToUserResponse, times(1)).fromUserToUserResponse(testUser);
    }

    @Test
    public void testGetAllUserByIdAsNonExistingUser() {
        // given
        User adminUser = new User();
        adminUser.setId("123");
        adminUser.setRole(Role.ADMIN);

        User testUser = new User();
        testUser.setId("456");
        testUser.setEmail("test@example.com");
        testUser.setModifiedDate(DateUtil.now());
        testUser.setCreatedDate(DateUtil.now());

        testUser.setRole(Role.MEMBER);

        UserResponse userResponse = new UserResponse();
        userResponse.setRole(testUser.getRole());
        userResponse.setEmail(testUser.getEmail());
        userResponse.setName(testUser.getName());
        userResponse.setId(testUser.getId());

        when(userRepository.findById("456")).thenReturn(Optional.empty());
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        assertThrows(BusinessException.class, () ->  userService.getUserById("456", adminUser.getId()));
    }

    @Test
    public void testGetUserByIdWithForbiddenError() {
        // given
        String id = "admin123";
        String userId = "admin123";

        // when + then
        assertThrows(BusinessException.class, () -> userService.getUserById(id, userId));
    }

    @Test
    public void testGetUserByIdWithUnauthorizedError() {
        // given
        String id = "user123";
        String userId = "user456";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(BusinessException.class, () -> userService.getUserById(id, userId));
    }

    @Test
    public void testGetInvoicesByUserId() {
        // given
        User adminUser = new User();
        adminUser.setId("123");
        adminUser.setRole(Role.ADMIN);


        User testUser = new User();
        testUser.setId("456");
        testUser.setEmail("test@example.com");
        testUser.setModifiedDate(DateUtil.now());
        testUser.setCreatedDate(DateUtil.now());

        testUser.setRole(Role.MEMBER);

        when(userRepository.findById("456")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("123")).thenReturn(Optional.of(adminUser));

        List<Invoice> invoiceList = new ArrayList<>();
        invoiceList.add(new Invoice());
        invoiceList.add(new Invoice());
        Page<Invoice> invoices = new PageImpl<>(invoiceList, PageRequest.of(0, 2), 2);

        when(invoiceRepository.findAllInvoicesByUserId(PageRequest.of(0, 2),"456")).thenReturn(invoices);

        when(fromInvoiceToInvoiceResponse.setterFromInvoiceToInvoiceResponse(any())).thenReturn(new InvoiceResponse());

        // when
        Page<InvoiceResponse> result = userService.getInvoicesByUserId(PageRequest.of(0, 2), "456", "123");

        // then
        verify(userRepository).findById("123");
        verify(invoiceRepository).findAllInvoicesByUserId(PageRequest.of(0, 2),"456");
        verify(fromInvoiceToInvoiceResponse, times(2)).setterFromInvoiceToInvoiceResponse(any());

        // additional assertions on result
        assert(result.getTotalElements() == 2);
        assert(result.getContent().size() == 2);
    }

    @Test
    public void testGetAllInvoicesByIdAsNonExistingUser() {
        // given
        User adminUser = new User();
        adminUser.setId("123");
        adminUser.setRole(Role.ADMIN);

        User testUser = new User();
        testUser.setId("456");
        testUser.setEmail("test@example.com");
        testUser.setModifiedDate(DateUtil.now());
        testUser.setCreatedDate(DateUtil.now());

        testUser.setRole(Role.MEMBER);

        UserResponse userResponse = new UserResponse();
        userResponse.setRole(testUser.getRole());
        userResponse.setEmail(testUser.getEmail());
        userResponse.setName(testUser.getName());
        userResponse.setId(testUser.getId());

        when(userRepository.findById("456")).thenReturn(Optional.empty());
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        assertThrows(BusinessException.class, () ->  userService.getInvoicesByUserId(PageRequest.of(0, 2), "456", "123"));
    }

    @Test
    public void testGetAllInvoicesByIdAsNonAdminUser() {
        // given
        User memberUser = new User();
        memberUser.setId("123");
        memberUser.setRole(Role.MEMBER);

        when(userRepository.findById(memberUser.getId())).thenReturn(Optional.of(memberUser));

        assertThrows(BusinessException.class, () ->  userService.getInvoicesByUserId(PageRequest.of(0, 2), "456", "123"));
    }

    @Test
    public void testSubscribe() {
        // given
        String userId = "1";
        String subId = "2";
        String currentUserId = "1";

        User user = new User();
        user.setId(userId);
        user.setVerified(true);
        user.setRole(Role.MEMBER);
        user.setPasswordHash("42780490294");
        user.setName("user1345");
        user.setLastName("userLastName");

        Subscription subscription = new Subscription();
        subscription.setId(subId);
        subscription.setName("Basic Subscription");
        subscription.setDuration(12);
        subscription.setMonthlyFee(10);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));
        Mockito.when(contractRepository.findContractRecordByUserIdAndIsActive(userId)).thenReturn(null);

        // when
        userService.subscribe(userId, subId, currentUserId);

        // then
        Mockito.verify(userRepository).save(user);
        Mockito.verify(contractRepository).save(captor.capture());

        ContractRecord contractRecord = captor.getValue();

        assertEquals(contractRecord.getUser_fk(),user);
        assertEquals(contractRecord.getDuration(),subscription.getDuration());
        assertEquals(contractRecord.getMonthlyFee(), subscription.getMonthlyFee());
        assertTrue(contractRecord.isActive());
        assertEquals(contractRecord.getName(), subscription.getName());
    }

    @Test
    public void testSubscribeWhenSubscribingToNewSubLessInDuration() {
        // given
        String userId = "1";
        String subId = "2";
        String currentUserId = "1";

        User user = new User();
        user.setId(userId);
        user.setVerified(true);
        user.setRole(Role.MEMBER);
        user.setPasswordHash("42780490294");
        user.setName("user1345");
        user.setLastName("userLastName");

        Subscription subscription = new Subscription();
        subscription.setId(subId);
        subscription.setName("Basic Subscription");
        subscription.setDuration(12);
        subscription.setMonthlyFee(10);

        ContractRecord contractRecord = new ContractRecord();
        contractRecord.setActive(true);
        contractRecord.setUser_fk(user);
        contractRecord.setDuration(15);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));
        Mockito.when(contractRepository.findContractRecordByUserIdAndIsActive(userId)).thenReturn(contractRecord);

        // when + then
        BusinessException e = assertThrows(BusinessException.class, () -> userService.subscribe(userId, subId, currentUserId));
        assertEquals(e.getMessage(), "You cannot subscribe to a deal that is less or equal in duration");
    }
    @Test
    public void testSubscribeParametersAreNotEqual() {
        BusinessException e = assertThrows(BusinessException.class, () -> userService.subscribe("123", "345", "678"));
        assertEquals(e.getMessage(), "Not allowed");
    }

    @Test
    public void testSubscribeWhenUserDoesNotExist() {
        when(userRepository.findById("678")).thenReturn(Optional.empty());
        BusinessException e = assertThrows(BusinessException.class, () -> userService.subscribe("678", "345", "678"));
        assertEquals(e.getMessage(), "Account does not exist");
    }

    @Test
    public void testSubscribeWhenUserIsNotVerified() {
        User user = new User();
        user.setVerified(false);
        when(userRepository.findById("678")).thenReturn(Optional.of(user));
        BusinessException e = assertThrows(BusinessException.class, () -> userService.subscribe("678", "345", "678"));
        assertEquals(e.getMessage(), "Account is not verified");
    }

    @Test
    public void testSubscribeWhenSubDoesNotExist() {
        User user = new User();
        user.setVerified(true);
        when(subscriptionRepository.findById(anyString())).thenReturn(Optional.empty());
        when(userRepository.findById("678")).thenReturn(Optional.of(user));
        BusinessException e = assertThrows(BusinessException.class, () -> userService.subscribe("678", "345", "678"));
        assertEquals(e.getMessage(), "Subscription with provided id does not exist");
    }

    @Test
    public void testSubscribeWhenUserHasRecord() {
        // given
        String userId = "1";
        String subId = "2";
        String currentUserId = "1";

        User user = new User();
        user.setId(userId);
        user.setVerified(true);
        user.setRole(Role.MEMBER);
        user.setPasswordHash("42780490294");
        user.setName("user1345");
        user.setLastName("userLastName");

        Subscription subscription = new Subscription();
        subscription.setId(subId);
        subscription.setName("Basic Subscription");
        subscription.setDuration(12);
        subscription.setMonthlyFee(10);

        ContractRecord contractRecord = new ContractRecord();
        contractRecord.setActive(true);
        contractRecord.setUser_fk(user);
        contractRecord.setDuration(10);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));
        Mockito.when(contractRepository.findContractRecordByUserIdAndIsActive(userId)).thenReturn(contractRecord);

        // when
        userService.subscribe(userId, subId, currentUserId);

        // then
        Mockito.verify(userRepository).save(user);
        Mockito.verify(contractRepository).save(captor.capture());
        assertFalse(contractRecord.isActive());
    }
}
