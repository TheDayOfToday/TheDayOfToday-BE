package com.example.thedayoftoday.domain.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class Swagger {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("The Day of Today API")
                        .version("1.0")
                        .description("The Day of Today 프로젝트의 API 명세서입니다."))
                .servers(List.of(
                        new Server().url("https://thedayoftoday.kro.kr").description("배포 서버"),
                        new Server().url("http://localhost:8080").description("로컬 서버")
                ));
    }
}
