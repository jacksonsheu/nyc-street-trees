package com.nyctrees.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Central web-layer configuration for HTTP clients and CORS behavior.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] allowedOrigins;

    /**
     * Creates the config with the allowed CORS origins for API access.
     *
     * @param allowedOrigins comma-separated list of allowed origin patterns, configured via
     *                        {@code app.cors.allowed-origins} (env var {@code APP_CORS_ALLOWED_ORIGINS}).
     *                        Defaults to common local dev origins only.
     */
    public WebConfig(
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}") String[] allowedOrigins
    ) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Creates a reusable {@link RestClient.Builder} bean for outbound HTTP calls.
     *
     * @return configured RestClient builder
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Creates the shared {@link ObjectMapper} used for JSON parsing.
     *
     * @return ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Enables cross-origin access for API routes, restricted to configured trusted origins
     * (e.g. the deployed frontend domain) rather than any origin.
     *
     * @param registry Spring CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Scope permissive CORS only to API endpoints, and only to trusted origins.
        registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedOriginPatterns(allowedOrigins);
    }
}
