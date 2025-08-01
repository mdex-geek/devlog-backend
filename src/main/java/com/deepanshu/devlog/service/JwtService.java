package com.deepanshu.devlog.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;



import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;

import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtService {

    private final Key SECRET_KEY;
    public JwtService(@Value("${jwt.secret}") String base64Secret) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }
    

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60 * 60); // 1 hour
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder().claims().add(claims).subject(username).issuedAt(now).expiration(expiry).and().signWith(getSignKey()).compact();
    }

    public String extractUsername(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(getSignKey())
                .build();

        Jws<Claims> claims = parser.parseSignedClaims(token);
        return claims.getPayload().getSubject();
    }

   

    private SecretKey getSignKey() throws WeakKeyException {
        try {
            byte[] decodedKey = SECRET_KEY.getEncoded();
            return Keys.hmacShaKeyFor(decodedKey);
        } catch (WeakKeyException e) {
            throw e;
        }
    }

    public boolean isTokenValid(String token, String username) {
        String extracted = extractUsername(token);
    return extracted.equals(username);
    }
}
