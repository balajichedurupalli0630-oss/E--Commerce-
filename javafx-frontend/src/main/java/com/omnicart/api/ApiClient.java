package com.omnicart.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.omnicart.config.AppConfig;
import com.omnicart.config.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(AppConfig.CONNECT_TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private HttpRequest.Builder createRequestBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(AppConfig.REQUEST_TIMEOUT_SECONDS))
                .header("Accept", "application/json");

        String token = SessionManager.getInstance().getAuthToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        if (SessionManager.getInstance().getCurrentUser() != null) {
            builder.header("x-user-id", SessionManager.getInstance().getCurrentUser().getId());
        }

        return builder;
    }

    public <T> CompletableFuture<T> getAsync(String url, Class<T> responseType) {
        HttpRequest request = createRequestBuilder(url).GET().build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            return objectMapper.readValue(response.body(), responseType);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                    }
                });
    }

    public <T> CompletableFuture<T> getAsync(String url, TypeReference<T> typeRef) {
        HttpRequest request = createRequestBuilder(url).GET().build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            return objectMapper.readValue(response.body(), typeRef);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                    }
                });
    }

    public <T> CompletableFuture<T> postAsync(String url, Object body, Class<T> responseType) {
        try {
            String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpRequest request = createRequestBuilder(url)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            try {
                                if (responseType == Void.class || response.body().isBlank()) {
                                    return null;
                                }
                                return objectMapper.readValue(response.body(), responseType);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
                            }
                        } else {
                            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public <T> CompletableFuture<T> postAsync(String url, Object body, TypeReference<T> typeRef) {
        try {
            String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpRequest request = createRequestBuilder(url)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            try {
                                return objectMapper.readValue(response.body(), typeRef);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
                            }
                        } else {
                            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public <T> CompletableFuture<T> putAsync(String url, Object body, Class<T> responseType) {
        try {
            String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
            HttpRequest request = createRequestBuilder(url)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            try {
                                return objectMapper.readValue(response.body(), responseType);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
                            }
                        } else {
                            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public <T> CompletableFuture<T> deleteAsync(String url, Class<T> responseType) {
        HttpRequest request = createRequestBuilder(url).DELETE().build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            if (responseType == Void.class || response.body().isBlank()) {
                                return null;
                            }
                            return objectMapper.readValue(response.body(), responseType);
                        } catch (Exception e) {
                            return null;
                        }
                    } else {
                        throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                    }
                });
    }
}
