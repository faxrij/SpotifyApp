package com.example.spotifyproject.helper;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenHelper {
    private static final String JWT_SECRET = "JWT_SECRET";

    public String generateTokenForMember() {

        String secretKey = System.getenv(JWT_SECRET);

        byte[] signingKey = secretKey.getBytes();
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, signingKey)
                .setHeaderParam("type", "JWT")
                .setSubject("39d525c8-25a5-4b7c-b6e1-6aa0132cf104")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 864000000))
                .compact();
    }

    public String generateTokenForAdmin() {

        String secretKey = System.getenv(JWT_SECRET);

        byte[] signingKey = secretKey.getBytes();
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, signingKey)
                .setHeaderParam("type", "JWT")
                .setSubject("39d525c8-25a5-4b7c-b6e1-6aa0132cf102")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 864000000))
                .compact();
    }

}
