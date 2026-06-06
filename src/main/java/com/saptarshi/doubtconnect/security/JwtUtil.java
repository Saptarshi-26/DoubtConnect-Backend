package com.saptarshi.doubtconnect.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {


    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private final long EXPIRATION_TIME =
            1000 * 60 * 60;

    private SecretKey getSigningKey(){
        byte[] keyBytes =
                SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Date extractExpiration(String token){
        Claims claims =
                Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

        return claims.getExpiration();
    }

    public String generateToken(String username){
        return Jwts.builder()
                .subject(username)
                .signWith(getSigningKey())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();

    }

    public String extractUsername(String token){
        Claims claims =
                Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
        return claims.getSubject();

    }

    public boolean validateToken(
            String token,
            UserDetails userDetails){

        String username =
                extractUsername(token);

        return username.equals(
                userDetails.getUsername())
                &&
                !extractExpiration(token)
                        .before(new Date());
    }
}
