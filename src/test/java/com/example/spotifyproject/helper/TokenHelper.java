package com.example.spotifyproject.helper;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class TokenHelper {
    private static final String JWT_SECRET = "JWT_SECRET";

    public String generateTokenForMember() {

        String secretKey = System.getenv(JWT_SECRET);

        byte[] signingKey = Decoders.BASE64.decode(secretKey);
        Key keys = Keys.hmacShaKeyFor(signingKey);
        return Jwts.builder()
                .signWith(keys, SignatureAlgorithm.HS512)
                .setHeaderParam("type", "JWT")
                .setSubject("39d525c8-25a5-4b7c-b6e1-6aa0132cf104")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 864000000))
                .compact();
    }

    public String generateTokenForAdmin() {

        String secretKey = System.getenv(JWT_SECRET);

        byte[] signingKey = Decoders.BASE64.decode(secretKey);
        Key keys = Keys.hmacShaKeyFor(signingKey);

        return Jwts.builder()
                .signWith(keys, SignatureAlgorithm.HS512)
                .setHeaderParam("type", "JWT")
                .setSubject("39d525c8-25a5-4b7c-b6e1-6aa0132cf102")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 864000000))
                .compact();
    }
}