package com.wexapp.purchaseapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Purchase Application API")
                        .version("1.0.0")
                        .description("REST API for managing purchases with currency conversion. " +
                                "The API provides endpoints for creating, retrieving, and deleting purchases, " +
                                "as well as converting purchase amounts to different currencies using exchange rates " +
                                "from the U.S. Treasury Fiscal Data API.")
                        .contact(new Contact()
                                .name("WexApp Support")
                                .email("support@wexapp.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"))
                .components(new Components()
                        .addSecuritySchemes("ApiKeyAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("API Key for authentication. Can also be provided as query parameter 'apiKey'.")));
    }
}

