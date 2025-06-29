package com.quodex.Invizo.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Secret key from application.properties
    @Value("${jwt.secret}")
    private String secret;

    // Expiration time in milliseconds (e.g., 1 day)
    @Value("${jwt.expiration}")
    private long expiration;

    // Create a signing key from the secret string

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    //  Generate token using UserDetails (email, roles etc.)

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Optional: add custom claims like roles
        claims.put("roles", userDetails.getAuthorities());

        return createToken(claims, userDetails.getUsername());
    }

    // Build the JWT token

    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)                                 // Set custom claims (optional)
                .setSubject(username)                              // Main subject of token (username/email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Token issue time
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Expiry time
                .signWith(getSignKey(), SignatureAlgorithm.HS256)  // Sign using HS256
                .compact();                                        // Return the final token string
    }

    //Extract username/email from token

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Generic method to extract any claim

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Validate token (username must match & not expired)

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Check if token is expired

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract expiration date

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Parse and get all claims from token

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
