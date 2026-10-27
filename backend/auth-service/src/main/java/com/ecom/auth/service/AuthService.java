package com.ecom.auth.service;

import com.ecom.dto.CreateUserRequest;
import com.ecom.dto.UserDTO;
import com.ecom.event.UserCreatedEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${clerk.secret.key:}")
    private String clerkSecretKey;

    // Local fallback store for development
    private final Map<String, UserDTO> localUserStore = new ConcurrentHashMap<>();

    public AuthService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        initMockUsers();
    }

    private void initMockUsers() {
        UserDTO admin = UserDTO.builder()
                .id("user_admin_01")
                .username("admin")
                .firstName("System")
                .lastName("Admin")
                .emailAddresses(List.of(Map.of("email_address", "admin@ecom.com", "id", "email_01")))
                .publicMetadata(Map.of("role", "admin"))
                .build();
        localUserStore.put(admin.getId(), admin);
    }

    public List<UserDTO> getUserList() {
        if (clerkSecretKey != null && !clerkSecretKey.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(clerkSecretKey);
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        "https://api.clerk.com/v1/users",
                        HttpMethod.GET,
                        entity,
                        String.class
                );
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return objectMapper.readValue(response.getBody(), new TypeReference<List<UserDTO>>() {});
                }
            } catch (Exception e) {
                log.warn("Clerk API call failed, falling back to local user store: {}", e.getMessage());
            }
        }
        return new ArrayList<>(localUserStore.values());
    }

    public UserDTO getUser(String id) {
        if (clerkSecretKey != null && !clerkSecretKey.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(clerkSecretKey);
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        "https://api.clerk.com/v1/users/" + id,
                        HttpMethod.GET,
                        entity,
                        String.class
                );
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return objectMapper.readValue(response.getBody(), UserDTO.class);
                }
            } catch (Exception e) {
                log.warn("Clerk API getUser failed: {}", e.getMessage());
            }
        }
        return localUserStore.get(id);
    }

    public UserDTO createUser(CreateUserRequest request) {
        String email = (request.getEmailAddress() != null && !request.getEmailAddress().isEmpty())
                ? request.getEmailAddress().get(0)
                : "user@example.com";

        UserDTO newUser = UserDTO.builder()
                .id("user_" + UUID.randomUUID().toString().substring(0, 8))
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .emailAddresses(List.of(Map.of("email_address", email, "id", "email_" + UUID.randomUUID())))
                .publicMetadata(Map.of("role", "user"))
                .build();

        if (clerkSecretKey != null && !clerkSecretKey.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(clerkSecretKey);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        "https://api.clerk.com/v1/users",
                        HttpMethod.POST,
                        entity,
                        String.class
                );
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    newUser = objectMapper.readValue(response.getBody(), UserDTO.class);
                }
            } catch (Exception e) {
                log.warn("Clerk API createUser failed, using local registration: {}", e.getMessage());
            }
        }

        localUserStore.put(newUser.getId(), newUser);

        // Publish user.created to Kafka
        try {
            UserCreatedEvent event = UserCreatedEvent.builder()
                    .username(newUser.getUsername())
                    .email(email)
                    .build();
            kafkaTemplate.send("user.created", event);
            log.info("Published user.created event to Kafka for user {}", newUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to publish user.created event", e);
        }

        return newUser;
    }

    public UserDTO deleteUser(String id) {
        UserDTO removed = localUserStore.remove(id);
        if (clerkSecretKey != null && !clerkSecretKey.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(clerkSecretKey);
                HttpEntity<?> entity = new HttpEntity<>(headers);
                restTemplate.exchange(
                        "https://api.clerk.com/v1/users/" + id,
                        HttpMethod.DELETE,
                        entity,
                        String.class
                );
                log.info("Deleted user {} from Clerk", id);
            } catch (Exception e) {
                log.warn("Clerk deleteUser failed: {}", e.getMessage());
            }
        }
        return removed;
    }
}
