package com.nyctrees.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata configuration. The generated spec (served at
 * {@code /v3/api-docs}) is the single source of truth for the API contract:
 * it is exported into {@code shared/contracts/} and used to generate
 * TypeScript types for client apps instead of hand-duplicating DTOs.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Describes the API for the generated OpenAPI document.
     *
     * @return OpenAPI metadata bean
     */
    @Bean
    public OpenAPI nycTreesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NYC Street Trees API")
                        .description("Endpoints for discovering NYC street trees and recording "
                                + "user interactions (maintenance requests, waterings).")
                        .version("v1")
                        .contact(new Contact().name("NYC Trees POC")));
    }
}
