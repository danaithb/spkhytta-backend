//Source: https://www.restack.io/p/spring-boot-answer-cors-permit-all
package com.bookingapp.cabin.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // For API-ruter
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000", "https://spkhytta.web.app")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);

                // For admin-ruter
                registry.addMapping("/admin/**")
                        .allowedOrigins("http://localhost:3000", "https://spkhytta.web.app")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
