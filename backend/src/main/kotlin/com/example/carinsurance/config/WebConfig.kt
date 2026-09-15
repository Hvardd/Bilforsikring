package com.example.carinsurance.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(@param:Value("\${app.frontend-origin}") private val frontendOrigin: String) :
    WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins(frontendOrigin)
            .allowedMethods("POST")
            .allowedHeaders("Content-Type", "X-Correlation-ID")
            .exposedHeaders("X-Correlation-ID")
    }
}
