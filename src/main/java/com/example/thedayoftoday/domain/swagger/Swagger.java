package com.example.thedayoftoday.domain.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Swagger {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("The Day of Today API")
                        .version("1.0")
                        .description("The Day of Today 프로젝트의 API 명세서입니다."));
    }
}
