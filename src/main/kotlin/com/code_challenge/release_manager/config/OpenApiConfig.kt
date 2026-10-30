package com.code_challenge.release_manager.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun releaseManagerOpenApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("Release Manager API")
            .version("v1")

    )
}
