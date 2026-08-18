package com.borrowbox.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI borrowBoxOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BorrowBox REST API")
                        .description("Peer-to-Peer Item Borrowing & Lending Platform Backend Services. " +
                                "Supports authentication, item catalog, date-range availability checks, " +
                                "concurrency-safe request processing, multi-stage handover condition tracking, " +
                                "real-time chat, weighted reputation scoring, disputes, and administrative operations.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BorrowBox Engineering")
                                .email("support@borrowbox.local"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
