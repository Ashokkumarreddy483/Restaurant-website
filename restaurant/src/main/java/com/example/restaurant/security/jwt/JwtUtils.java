package com.example.restaurant.security.jwt;

import com.example.restaurant.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys; // Correct import for Keys
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // <<--- ADD THIS IMPORT
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors; // <<--- ADD THIS IMPORT

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${restaurant.app.jwtSecret}")
    private String jwtSecretString; // Renamed to avoid confusion with Key type

    @Value("${restaurant.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        // Ensure your jwtSecretString is strong enough (at least 256 bits for HS256)
        this.key = Keys.hmacShaKeyFor(jwtSecretString.getBytes());
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        // Add custom claims if needed, for example:
        claims.put("id", userPrincipal.getId());
        claims.put("email", userPrincipal.getEmail());
        if (userPrincipal.getFirstName() != null) {
            claims.put("firstName", userPrincipal.getFirstName());
        }
        if (userPrincipal.getLastName() != null) {
            claims.put("lastName", userPrincipal.getLastName());
        }
        // Correctly extract roles as a list of strings
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // Use the imported GrantedAuthority
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims) // Set all claims
                .setSubject(userPrincipal.getUsername()) // 'sub' claim is standard for username
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // This method might be useful if you need to generate a token without a full Authentication object,
    // e.g., for password reset tokens, but it won't have all the custom claims from UserDetailsImpl
    public String generateTokenFromUsername(String username) {
        return Jwts.builder().setSubject(username).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // You can also add a method to get all claims if needed
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }


    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) { // Catching generic JwtException covers SignatureException, MalformedJwtException, etc.
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        // Removed redundant specific catches as JwtException is their superclass
        // catch (MalformedJwtException e) { logger.error("Invalid JWT token: {}", e.getMessage()); }
        // catch (ExpiredJwtException e) { logger.error("JWT token is expired: {}", e.getMessage()); }
        // catch (UnsupportedJwtException e) { logger.error("JWT token is unsupported: {}", e.getMessage()); }
        // catch (IllegalArgumentException e) { logger.error("JWT claims string is empty: {}", e.getMessage()); }
        return false;
    }
}