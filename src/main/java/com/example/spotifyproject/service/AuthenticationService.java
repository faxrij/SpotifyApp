package com.example.spotifyproject.service;

import com.example.spotifyproject.entity.Role;
import com.example.spotifyproject.entity.User;
import com.example.spotifyproject.exception.BusinessException;
import com.example.spotifyproject.exception.ErrorCode;
import com.example.spotifyproject.model.request.auth.*;
import com.example.spotifyproject.model.response.LoginResponse;
import com.example.spotifyproject.repository.UserRepository;
import com.example.spotifyproject.security.JwtService;
import com.example.spotifyproject.service.client.EmailClient;
import com.example.spotifyproject.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EmailClient emailClient;

    public String getAuthenticatedUserId() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal.equals("anonymousUser")) {
            throw new BusinessException(ErrorCode.unauthorized, "Unauthorized user!");
        }
        return principal;
    }

    public LoginResponse login(LoginRequest loginRequest){
        User user = userRepository.findUserByEmail(loginRequest.getEmail()).orElseThrow(
                () -> new BusinessException(ErrorCode.account_missing, "There is no user like that")
        );
        if (!passwordEncoder.matches(loginRequest.getPassword(),user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.password_mismatch, "Wrong Credentials");
        }
        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .token(jwtService.createToken(user.getId()))
                .id(user.getId())
                .build();
    }

    public void register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findUserByEmail(registerRequest.getEmail());
        if (optionalUser.isPresent()) {
            throw new BusinessException(ErrorCode.account_already_exists, "User Already Exists");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setLastName(registerRequest.getLastName());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.GUEST);
        user.setVerified(false);
        user.setEmail(registerRequest.getEmail());
        user.setCreatedDate(ZonedDateTime.now());
        user.setModifiedDate(ZonedDateTime.now());

        userRepository.save(user);
    }

    public void verify(EmailVerificationRequest body) {

        Optional<User> optionalUser = userRepository.findUserByEmail(body.getEmail());
        if (!optionalUser.isPresent()) {
            throw new BusinessException(ErrorCode.account_already_exists, "There is no user like that");
        }
        User user = optionalUser.get();

        if (user.getVerificationCodeExpiredDate().isBefore(DateUtil.now())) {
            throw new BusinessException(ErrorCode.code_expired, "Expired");
        }

        if (!user.getVerificationCode().equals(body.getVerificationCode())) {
            throw new BusinessException(ErrorCode.code_mismatch, "No Match");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiredDate(null);

        userRepository.save(user);

    }

    public void verifyEmail(EmailRequest body) {
        Optional<User> optionalUser = userRepository.findUserByEmail(body.getEmail());
        if (!optionalUser.isPresent()) {
            throw new BusinessException(ErrorCode.account_already_exists, "There is no user like that");
        }
        User user = optionalUser.get();

        if (user.isVerified()) {
            throw new BusinessException(ErrorCode.account_already_verified, "User is already verified");
        }

        String verificationCode = RandomStringUtils.randomAlphanumeric(24);
        ZonedDateTime verificationCodeExpiredDate = DateUtil.now().plusDays(1);

        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiredDate(verificationCodeExpiredDate);
        userRepository.save(user);

        emailClient.sendVerificationEmail(user);

    }

    public void recover(EmailRecoveryRequest body) {
        Optional<User> optionalUser = userRepository.findUserByEmail(body.getEmail());
        if (!optionalUser.isPresent()) {
            throw new BusinessException(ErrorCode.account_already_exists, "There is no user like that");
        }
        User user = optionalUser.get();

        if (user.getRecoveryCodeExpiredDate().isBefore(DateUtil.now())) {
            throw new BusinessException(ErrorCode.code_expired, "Expired");
        }

        if (!user.getRecoveryCode().equals(body.getRecoveryCode())) {
            throw new BusinessException(ErrorCode.code_mismatch, "No Match");
        }

        user.setPasswordHash(passwordEncoder.encode(body.getNewPassword()));
        user.setRecoveryCode(null);
        user.setRecoveryCodeExpiredDate(null);
        userRepository.save(user);

    }

    public void sendRecoveryEmail(EmailRequest body) {
        Optional<User> optionalUser = userRepository.findUserByEmail(body.getEmail());

        if (!optionalUser.isPresent()) {
            throw new BusinessException(ErrorCode.account_already_exists, "There is no user like that");
        }

        User user = optionalUser.get();

        String recoveryCode = RandomStringUtils.randomAlphanumeric(24);
        ZonedDateTime recoveryCodeExpiredDate = DateUtil.now().plusDays(1);

        user.setRecoveryCode(recoveryCode);
        user.setRecoveryCodeExpiredDate(recoveryCodeExpiredDate);
        userRepository.save(user);

        emailClient.sendRecoveryEmail(user);
    }
}