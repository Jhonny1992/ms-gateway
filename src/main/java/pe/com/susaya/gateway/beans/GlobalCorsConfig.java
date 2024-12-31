package pe.com.susaya.gateway.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:4200"); // Permitir Angular
        corsConfiguration.addAllowedMethod("*"); // Permitir todos los métodos (GET, POST, etc.)
        corsConfiguration.addAllowedHeader("*"); // Permitir todos los headers
        corsConfiguration.setAllowCredentials(true); // Si necesitas cookies

        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();
        corsConfigSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(corsConfigSource);
    }
}
