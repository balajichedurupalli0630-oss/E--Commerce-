package com.ecom.auth.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final String secret;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret:omnicart-production-grade-jwt-secret-key-256-bit-minimum-security-standard}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(String userId, String username, String email, String role, String firstName, String lastName) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + (expirationMs / 1000);

            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", userId);
            payload.put("username", username);
            payload.put("email", email);
            payload.put("role", role != null ? role : "user");
            payload.put("firstName", firstName != null ? firstName : "");
            payload.put("lastName", lastName != null ? lastName : "");
            payload.put("iat", now);
            payload.put("exp", exp);

            String encodedHeader = URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(header));
            String encodedPayload = URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(payload));
            String dataToSign = encodedHeader + "." + encodedPayload;

            String signature = sign(dataToSign, secret);
            return dataToSign + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String dataToSign = parts[0] + "." + parts[1];
            String expectedSignature = sign(dataToSign, secret);

            // Constant-time signature comparison
            if (!MessageDigest.isEqual(parts[2].getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            Map<String, Object> claims = extractClaims(token);
            if (claims == null) return false;

            Number exp = (Number) claims.get("exp");
            if (exp == null) return false;

            long now = Instant.now().getEpochSecond();
            return exp.longValue() > now;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> extractClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            byte[] decodedBytes = URL_DECODER.decode(parts[1]);
            return OBJECT_MAPPER.readValue(decodedBytes, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    public String extractUserId(String token) {
        Map<String, Object> claims = extractClaims(token);
        return claims != null ? (String) claims.get("sub") : null;
    }

    public String extractUsername(String token) {
        Map<String, Object> claims = extractClaims(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    public String extractRole(String token) {
        Map<String, Object> claims = extractClaims(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    private String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return URL_ENCODER.encodeToString(rawHmac);
    }
}
