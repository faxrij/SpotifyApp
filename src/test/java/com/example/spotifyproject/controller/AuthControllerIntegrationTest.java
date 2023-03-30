package com.example.spotifyproject.controller;

import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.model.request.auth.*;
import com.example.spotifyproject.model.response.LoginResponse;
import com.example.spotifyproject.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class AuthControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Before
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void register_shouldReturnCreatedStatus() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("test");
        request.setLastName("test");
        ResponseEntity<Void> response = restTemplate.exchange("/auth/register", HttpMethod.POST, new HttpEntity<>(request), Void.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void register_shouldCreateNewUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("test");
        request.setLastName("test");
        restTemplate.exchange("/auth/register", HttpMethod.POST, new HttpEntity<>(request), Void.class);

        Optional<User> user = userRepository.findUserByEmail("test@example.com");
        assertTrue(user.isPresent());
        assertEquals("test@example.com", user.get().getEmail());
        assertTrue(passwordEncoder.matches("password", user.get().getPasswordHash()));
    }

    @Test
    public void verify_shouldVerifyUser() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setVerificationCode("code");
        user.setVerificationCodeExpiredDate(ZonedDateTime.now().plusDays(1));
        user.setVerified(false);
        user.setCreatedDate(ZonedDateTime.now());
        user.setModifiedDate(ZonedDateTime.now());
        userRepository.save(user);

        EmailVerificationRequest request = new EmailVerificationRequest();
        request.setEmail("test@example.com");
        request.setVerificationCode("code");

        // when
        ResponseEntity<Void> response = restTemplate.exchange("/auth/verify", HttpMethod.POST, new HttpEntity<>(request), Void.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Optional<User> verifiedUser = userRepository.findUserByEmail("test@example.com");
        assertTrue(verifiedUser.isPresent());
        assertTrue(verifiedUser.get().isVerified());
    }

    @Test
    public void sendVerificationEmail_shouldSendEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setCreatedDate(ZonedDateTime.now());
        user.setModifiedDate(ZonedDateTime.now());
        userRepository.save(user);

        EmailRequest request = new EmailRequest();
        request.setEmail("test@example.com");
        restTemplate.exchange("/api/v1/auth/verify/email", HttpMethod.POST, new HttpEntity<>(request), Void.class);

        // Assert that an email was sent
    }

    @Test
    public void recover_shouldRecoverPassword() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRecoveryCode("code");
        user.setRecoveryCodeExpiredDate(ZonedDateTime.now().plusDays(1));
        user.setCreatedDate(ZonedDateTime.now());
        user.setModifiedDate(ZonedDateTime.now());
        userRepository.save(user);

        EmailRecoveryRequest request = new EmailRecoveryRequest();
        request.setEmail("test@example.com");
        request.setRecoveryCode("code");
        request.setNewPassword("newpassword");
        restTemplate.exchange("/auth/recover", HttpMethod.POST, new HttpEntity<>(request), Void.class);

        Optional<User> recoveredUser = userRepository.findUserByEmail("test@example.com");
        assertTrue(recoveredUser.isPresent());
        assertTrue(passwordEncoder.matches("newpassword", recoveredUser.get().getPasswordHash()));
        assertNull(recoveredUser.get().getRecoveryCode());
        assertNull(recoveredUser.get().getRecoveryCodeExpiredDate());
    }

    @Test
    public void sendRecoveryEmail_shouldSendEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setCreatedDate(ZonedDateTime.now());
        user.setModifiedDate(ZonedDateTime.now());
        userRepository.save(user);

        EmailRequest request = new EmailRequest();
        request.setEmail("test@example.com");
        restTemplate.exchange("/auth/recover/email", HttpMethod.POST, new HttpEntity<>(request), Void.class);

        // Assert that an email was sent
    }

    @Test
    public void sendVerificationEmail_throwsException() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        user.setCreatedDate(ZonedDateTime.now());
        user.setModifiedDate(ZonedDateTime.now());
        userRepository.save(user);

        // when
        EmailRequest request = new EmailRequest();
        request.setEmail("test@example.com");
        ResponseEntity<Void> response = restTemplate.exchange("/auth/verify/email", HttpMethod.POST, new HttpEntity<>(request), Void.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // TODO: Assert that an email was sent
    }

    @Test
    public void login_shouldReturnToken() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setCreatedDate(ZonedDateTime.now());
        user.setModifiedDate(ZonedDateTime.now());
        userRepository.save(user);

        // when
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        ResponseEntity<LoginResponse> response = restTemplate.exchange("/auth/login", HttpMethod.POST, new HttpEntity<>(request), LoginResponse.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(Objects.requireNonNull(response.getBody()).getToken());
    }

}

