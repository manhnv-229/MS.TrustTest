package com.mstrust.exam.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/* ---------------------------------------------------
 * OpenAPI Configuration cho Swagger UI
 * Cấu hình thông tin API và JWT authentication
 * @author: K24DTCN210-NVMANH (15/11/2025 09:52)
 * --------------------------------------------------- */
@Configuration
public class OpenApiConfig {

    /* ---------------------------------------------------
     * Cấu hình OpenAPI với JWT Bearer authentication
     * @returns OpenAPI instance với security scheme
     * @author: K24DTCN210-NVMANH (15/11/2025 09:52)
     * --------------------------------------------------- */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(new Info()
                        .title("MS.TrustTest API")
                        .version("1.0")
                        .description("Exam Management System API with JWT Authentication"));
    }
}
