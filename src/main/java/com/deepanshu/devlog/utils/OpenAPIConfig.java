package com.deepanshu.devlog.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenAPIConfig {
@Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("DevLog API")
                .version("1.0.0")
                .description("API documentation for the DevLog Project Management System"));
    }
}
