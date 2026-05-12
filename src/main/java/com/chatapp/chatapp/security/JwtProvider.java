package com.chatapp.chatapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    // Must be at least 32 characters for HS256/HS512
    private final String SECRET_STRING = "Prattle_Super_Secret_Key_2026_Secure_And_Long_Enough";
    private final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    public String generateToken(String phoneNumber) {
        // 10 days
        long EXPIRATION_TIME = 864000000;
        return Jwts.builder()
                .subject(phoneNumber)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY)
                .compact();
    }

    public String getPhoneNumberFromToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY) // This replaces setSigningKey
                .build()         // Returns the JwtParser
                .parseSignedClaims(token) // This replaces parseClaimsJws
                .getPayload()    // This replaces getBody
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Log this: invalid signature, expired, etc.
            return false;
        }
    }
}