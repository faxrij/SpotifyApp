package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.Role;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.model.request.auth.*;
import com.example.spotifyproject.model.response.LoginResponse;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.security.JwtService;
import com.example.spotifyproject.service.client.EmailClient;
import com.example.spotifyproject.util.DateUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private AuthenticationService authenticationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;
    @Mock
    private EmailClient emailClient;
    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(passwordEncoder, userRepository, jwtService, emailClient);
    }

    @Test
    public void login_withUser_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("tester@example.com");
        loginRequest.setPassword("password");

        User user = new User();
        user.setId("123");
        user.setEmail("tester@example.com");
        user.setRole(Role.GUEST);
        user.setName("TESTER");
        user.setPasswordHash(passwordEncoder.encode("password"));

        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(),user.getPasswordHash())).thenReturn(true);

        LoginResponse loginResponse = authenticationService.login(loginRequest);
        assertEquals(loginResponse.getRole(), user.getRole());
        assertEquals(loginResponse.getName(), user.getName());
        assertEquals(loginResponse.getId(), user.getId());

    }
    @Test
    public void login_withMissingUser_ShouldThrowError() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("tester@example.com");
        loginRequest.setPassword("0000000");

        when(userRepository.findUserByEmail("tester@example.com")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> authenticationService.login(loginRequest));

    }

    @Test
    public void login_withWrongCredentials_ShouldThrowError() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("tester@example.com");
        loginRequest.setPassword("0000000");
        User user = new User();

        when(userRepository.findUserByEmail("tester@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(),user.getPasswordHash())).thenReturn(false);

        assertThrows(BusinessException.class, () -> authenticationService.login(loginRequest));

    }

    @Test
    public void getAuthenticatedUserId_withAuthenticatedUser_shouldReturnUserId() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String actualUserId = authenticationService.getAuthenticatedUserId();

        assertEquals("user@example.com", actualUserId);
    }

    @Test
    public void getAuthenticatedUserId_withAnonymousUser_shouldThrowException() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("anonymousUser", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(BusinessException.class, () -> authenticationService.getAuthenticatedUserId());
    }

    @Test
    public void register_withNewUser_shouldSaveUser() {
        // given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPassword("password");
        registerRequest.setEmail("john.doe@example.com");

        when(userRepository.findUserByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        // when
        authenticationService.register(registerRequest);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(registerRequest.getName(), savedUser.getName());
        assertEquals(registerRequest.getLastName(), savedUser.getLastName());
        assertEquals("encodedPassword", savedUser.getPasswordHash());
        assertEquals(Role.GUEST, savedUser.getRole());
        assertFalse(savedUser.isVerified());
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
    }


    @Test
    public void register_withExistingUser_shouldThrowException() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing.user@example.com");

        User existingUser = new User();
        existingUser.setEmail(registerRequest.getEmail());

        when(userRepository.findUserByEmail(registerRequest.getEmail())).thenReturn(Optional.of(existingUser));

        assertThrows(BusinessException.class, () -> authenticationService.register(registerRequest));
    }



    @Test
    public void verify_withValidVerificationCode_shouldVerifyUser() {
        String email = "john.doe@example.com";
        String verificationCode = RandomStringUtils.randomAlphanumeric(24);

        User user = new User();
        user.setEmail(email);
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiredDate(ZonedDateTime.now().plusDays(1));

        EmailVerificationRequest body = new EmailVerificationRequest();
        body.setEmail(email);
        body.setVerificationCode(verificationCode);

        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        authenticationService.verify(body);

        assertTrue(user.isVerified());
        assertNull(user.getVerificationCode());
        assertNull(user.getVerificationCodeExpiredDate());

        verify(userRepository).save(user);
    }

    @Test
    public void verify_whenThereIsNoUserLikeThat() {
        String email = "john.doe@example.com";
        EmailVerificationRequest body = new EmailVerificationRequest();
        body.setEmail(email);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authenticationService.verify(body));
    }

    @Test
    public void verify_whenThereIsExpiredVerificationDate() {
        String email = "john.doe@example.com";
        User user = new User();
        user.setVerificationCodeExpiredDate(ZonedDateTime.now().minusDays(1));
        user.setEmail(email);
        EmailVerificationRequest body = new EmailVerificationRequest();
        body.setEmail(email);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> authenticationService.verify(body));
    }

    @Test
    public void verify_whenThereIsMismatchesWith() {

        String email = "john.doe@example.com";
        User user = new User();
        user.setVerificationCodeExpiredDate(ZonedDateTime.now().plusDays(1));
        user.setVerificationCode("123");
        user.setEmail(email);

        EmailVerificationRequest body = new EmailVerificationRequest();
        body.setEmail(email);
        body.setVerificationCode("1234");
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> authenticationService.verify(body));
    }

    @Test
    public void verifyEmail_withUnverifiedUser_shouldGenerateVerificationCode() {
        // Given
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("john.doe@example.com");

        User user = new User();
        user.setVerified(false);
        user.setEmail(emailRequest.getEmail());
        when(userRepository.findUserByEmail(emailRequest.getEmail())).thenReturn(Optional.of(user));

        // When
        authenticationService.verifyEmail(emailRequest);

        // Then
        verify(userRepository).save(user);
        Assertions.assertNotNull(user.getVerificationCode());
        Assertions.assertTrue(user.getVerificationCode().matches("^[a-zA-Z0-9]+$"));
        ZonedDateTime expectedVerificationCodeExpiredDate = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1);
        Assertions.assertTrue(expectedVerificationCodeExpiredDate.minusMinutes(1).isBefore(user.getVerificationCodeExpiredDate()));
    }

    @Test
    public void verifyEmail_withVerifiedUser_shouldThrowBusinessException() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("john.doe@example.com");

        User user = new User();
        user.setVerified(true);
        user.setEmail(emailRequest.getEmail());

        when(userRepository.findUserByEmail(emailRequest.getEmail())).thenReturn(Optional.of(user));

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> authenticationService.verifyEmail(emailRequest));

        Assertions.assertEquals(exception.getErrorCode(), "account_already_verified");
    }

    @Test
    public void verifyEmail_withNonExistingUser_shouldThrowBusinessException() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("john.doe@example.com");

        when(userRepository.findUserByEmail(emailRequest.getEmail())).thenReturn(Optional.empty());

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> authenticationService.verifyEmail(emailRequest));

        Assertions.assertEquals(exception.getErrorCode(), "account_already_exists");
    }

    @Test
    public void recover_withValidRequest_shouldUpdateUserPassword() {
        String email = "john.doe@example.com";
        String recoveryCode = "ABCD1234";
        String newPassword = "newPassword";

        User user = new User();
        user.setEmail(email);
        user.setRecoveryCode(recoveryCode);
        user.setRecoveryCodeExpiredDate(ZonedDateTime.now().plusMinutes(5));

        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        EmailRecoveryRequest recoveryRequest = new EmailRecoveryRequest();
        recoveryRequest.setEmail(email);
        recoveryRequest.setRecoveryCode(recoveryCode);
        recoveryRequest.setNewPassword(newPassword);

        authenticationService.recover(recoveryRequest);

        verify(userRepository).save(user);
        Assertions.assertNull(user.getRecoveryCode());
        Assertions.assertNull(user.getRecoveryCodeExpiredDate());
        Assertions.assertEquals("encodedPassword", user.getPasswordHash());
    }

    @Test
    public void recover_withNonexistentUser_shouldThrowBusinessException() {
        String email = "john.doe@example.com";
        String recoveryCode = "ABCD1234";
        String newPassword = "newPassword";

        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        EmailRecoveryRequest recoveryRequest = new EmailRecoveryRequest();
        recoveryRequest.setEmail(email);
        recoveryRequest.setRecoveryCode(recoveryCode);
        recoveryRequest.setNewPassword(newPassword);

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> authenticationService.recover(recoveryRequest));

        Assertions.assertEquals("account_already_exists", exception.getErrorCode());
        Assertions.assertEquals("There is no user like that", exception.getMessage());
    }

    @Test
    public void recover_withExpiredRecoveryCode_shouldThrowBusinessException() {
        String email = "john.doe@example.com";
        String recoveryCode = "ABCD1234";
        String newPassword = "newPassword";

        User user = new User();
        user.setEmail(email);
        user.setRecoveryCode(recoveryCode);
        user.setRecoveryCodeExpiredDate(ZonedDateTime.now().minusMinutes(5));

        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(user));

        EmailRecoveryRequest recoveryRequest = new EmailRecoveryRequest();
        recoveryRequest.setEmail(email);
        recoveryRequest.setRecoveryCode(recoveryCode);
        recoveryRequest.setNewPassword(newPassword);

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> authenticationService.recover(recoveryRequest));

        Assertions.assertEquals("code_expired", exception.getErrorCode());
        Assertions.assertEquals("Expired", exception.getMessage());
    }

    @Test
    public void recover_withMismatchCode_shouldThrowBusinessException() {
        EmailRecoveryRequest recoveryRequest = new EmailRecoveryRequest();
        recoveryRequest.setEmail("jane.doe@example.com");
        recoveryRequest.setRecoveryCode("123456");
        recoveryRequest.setNewPassword("new_password");

        User user = new User();
        user.setEmail(recoveryRequest.getEmail());
        user.setRecoveryCode("654321");
        user.setRecoveryCodeExpiredDate(DateUtil.now().plusDays(1));

        when(userRepository.findUserByEmail(recoveryRequest.getEmail())).thenReturn(Optional.of(user));

        BusinessException exception = assertThrows(BusinessException.class, () -> authenticationService.recover(recoveryRequest));

        assertEquals("code_mismatch", exception.getErrorCode());
    }

    @Test
    public void sendRecoveryEmail_withExistingUser_shouldSetRecoveryCodeAndExpiredDate() {
        // Given
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("john.doe@example.com");

        User user = new User();
        user.setEmail(emailRequest.getEmail());

        when(userRepository.findUserByEmail(emailRequest.getEmail())).thenReturn(Optional.of(user));

        // When
        authenticationService.sendRecoveryEmail(emailRequest);

        // Then
        verify(userRepository).save(user);
        Assertions.assertNotNull(user.getRecoveryCode());
        Assertions.assertTrue(user.getRecoveryCode().matches("^[a-zA-Z0-9]+$"));
        Assertions.assertTrue(user.getRecoveryCodeExpiredDate().minusMinutes(1).isBefore(ZonedDateTime.now().plusDays(1)));
    }

    @Test
    public void sendRecoveryEmail_withNonExistingUser_shouldThrowBusinessException() {
        // Given
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("john.doe@example.com");

        when(userRepository.findUserByEmail(emailRequest.getEmail())).thenReturn(Optional.empty());

        // When + Then
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> authenticationService.sendRecoveryEmail(emailRequest));
        Assertions.assertEquals("account_already_exists", exception.getErrorCode());
        Assertions.assertEquals("There is no user like that", exception.getMessage());
    }
}