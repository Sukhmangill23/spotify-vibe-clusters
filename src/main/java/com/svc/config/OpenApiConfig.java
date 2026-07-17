package com.svc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vibeClustersOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spotify Vibe Clusters API")
                        .version("0.1.0")
                        .description("Clusters a user's Spotify library into vibe groups and serves track recommendations."));
    }
}
