package com.bookingapp.cabin.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@SpringBootApplication
public class CabinBookingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(CabinBookingAppApplication.class, args);
	}

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOrigins(List.of("https://spkhytta.web.app"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		System.out.println("✅ CORS FILTER FROM MAIN APP");

		return new CorsFilter(source);
	}
}
