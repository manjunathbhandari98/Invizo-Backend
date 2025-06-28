package com.quodex.Invizo.config;

import com.quodex.Invizo.jwt.JwtRequestFilter;
import com.quodex.Invizo.service.impl.AppUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Injecting the custom user details service that loads user data from DB
    private final AppUserDetailService appUserDetailService;

    private final JwtRequestFilter jwtRequestFilter;

    // Configures CORS to allow frontend apps (like React) to make API requests
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow these origins (frontend base URLs)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://192.168.1.10:5173"
        ));

        // Allow standard HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers
        config.setAllowedHeaders(List.of("*"));

        // Allow credentials like cookies or auth headers
        config.setAllowCredentials(true);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Provides a password encoder (BCrypt is secure and commonly used)
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // Configures the AuthenticationManager with our custom user service and password encoder
    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(appUserDetailService); // Use our service to load user
        authProvider.setPasswordEncoder(passwordEncoder()); // Use BCrypt to validate passwords
        return new ProviderManager(authProvider);
    }

    // Main security configuration for all API routes
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS using the config above
                .cors(Customizer.withDefaults())

                // Disable CSRF (we’re using JWTs instead of cookies)
                .csrf(AbstractHttpConfigurer::disable)

                // Define which endpoints are open or protected
                .authorizeHttpRequests(auth -> auth
                        // Allow unauthenticated access to login and encode APIs
                        .requestMatchers("/login", "/encode").permitAll()

                        // These endpoints require USER or ADMIN roles
                        .requestMatchers("/categories", "/items","/orders","/payments").hasAnyRole("USER", "ADMIN")

                        // These are restricted to ADMIN only
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // All other endpoints must be authenticated
                        .anyRequest().authenticated()
                )

                // We don't use sessions — this tells Spring to treat each request as stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Add the JWT filter before Spring's default UsernamePasswordAuthenticationFilter to process JWT tokens first
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Build and return the security filter chain
    }
}
