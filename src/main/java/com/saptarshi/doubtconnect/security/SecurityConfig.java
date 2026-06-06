package com.saptarshi.doubtconnect.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        return http

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers("/auth/**")
                        .permitAll()

                        // Anyone can view teachers
                        .requestMatchers(
                                HttpMethod.GET,
                                "/teacher/**")
                        .permitAll()

                        // Teacher only actions
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/teacher/**")
                        .hasRole("TEACHER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/teacher/**")
                        .hasRole("TEACHER")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/teacher/**")
                        .hasRole("TEACHER")

                        // Student endpoints
                        .requestMatchers("/student/**")
                        .hasRole("STUDENT")

                        // Everything else requires login
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}