package com.example.spotifyproject.service.client;

import com.example.spotifyproject.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@AllArgsConstructor
@Service
public class EmailClient {

    private JavaMailSender emailSender;

    public void sendVerificationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Email Verification");
        String url = " http://localhost:8080/verify/?code=" + user.getVerificationCode() + "&email=" + user.getEmail(); //TODO change url
        message.setText(url);
        emailSender.send(message);
    }

    public void sendRecoveryEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Recovery");
        String url = " http://localhost:8080/recover/?code=" + user.getRecoveryCode() + "&email=" + user.getEmail(); //TODO change url
        message.setText(url);
        emailSender.send(message);
    }
}
