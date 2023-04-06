package com.example.spotifyproject.security;

import com.example.spotifyproject.config.SecurityConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Date;

import java.security.Key;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final SecurityConfig securityConfig;

    @Autowired
    public JwtService(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public Authentication verifyToken(String token) {
        if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer ")) {
            try {
                byte[] signingKey = Decoders.BASE64.decode(securityConfig.getJwtSecret());
                Key keys = Keys.hmacShaKeyFor(signingKey);

                Jws<Claims> parsedToken = Jwts.parserBuilder()
                        .setSigningKey(keys).build()
                        .parseClaimsJws(token.replace("Bearer ", ""));

                String subject = parsedToken
                        .getBody()
                        .getSubject();

                if (StringUtils.isNotEmpty(subject)) {
                    return new UsernamePasswordAuthenticationToken(subject, null, null);
                }
            } catch (ExpiredJwtException exception) {
                logger.warn("Request to parse expired JWT : {} failed : {}", token, exception.getMessage());
            } catch (UnsupportedJwtException exception) {
                logger.warn("Request to parse unsupported JWT : {} failed : {}", token, exception.getMessage());
            } catch (MalformedJwtException exception) {
                logger.warn("Request to parse invalid JWT : {} failed : {}", token, exception.getMessage());
            } catch (IllegalArgumentException exception) {
                logger.warn("Request to parse empty or null JWT : {} failed : {}", token, exception.getMessage());
            }
        }
        return null;
    }

    public String createToken(String subject) {
        byte[] signingKey = Decoders.BASE64.decode(securityConfig.getJwtSecret());
        Key keys = Keys.hmacShaKeyFor(signingKey);
        return Jwts.builder()
                .signWith(keys, SignatureAlgorithm.HS512)
                .setHeaderParam("type", "JWT")
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 864000000))
                .compact();
    }
}
