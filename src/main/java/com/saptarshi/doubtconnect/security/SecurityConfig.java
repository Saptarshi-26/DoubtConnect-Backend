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

                .cors(cors -> {})

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/teacher-meeting/**")
                        .hasRole("TEACHER")

                        // -------------------- TEST DATA (REMOVE BEFORE PROD) --------------------

                        .requestMatchers("/test-data/**")
                        .permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Authentication
                        .requestMatchers("/auth/**")
                        .permitAll()

                        // -------------------- ADMIN --------------------

                        .requestMatchers("/user/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/student/all/**")
                        .hasRole("ADMIN")

                        // -------------------- PUBLIC TEACHER --------------------

                        .requestMatchers(
                                HttpMethod.GET,
                                "/teacher/getAll",
                                "/teacher/search",
                                "/teacher/{id}"
                        ).permitAll()


                        .requestMatchers(
                                HttpMethod.GET,
                                "/feedback/teacher/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/feedback/student/**"
                        )
                        .hasAnyRole("ADMIN", "STUDENT")

                        // -------------------- TEACHER --------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/teacher/getAllInternal"
                        ).hasRole("ADMIN")

                        .requestMatchers("/teacher/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers("/teacher-availability/student/**")
                        .hasAnyRole("ADMIN", "TEACHER", "STUDENT")

                        .requestMatchers("/teacher-availability/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(
                                "/google/verify"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/oauth/google/callback"
                        ).permitAll()

                        .requestMatchers("/reset-password/**").permitAll()

                        .requestMatchers("/oauth/google/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/session/accept/**",
                                "/session/reject/**"
                        )
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/session/teacher/**"
                        )
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/session-event/teacher/**"
                        )
                        .hasAnyRole("ADMIN", "TEACHER")

                        // -------------------- STUDENT --------------------

                        .requestMatchers("/student/**")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers("/slot-booking/**")
                        .hasAnyRole("ADMIN", "STUDENT", "TEACHER")

                        .requestMatchers("/payment/order/**")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers("/session/payment/**")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/session/save/**"
                        )
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/session/update/**"
                        )
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/session/delete/**"
                        )
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/session/student/**"
                        )
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/session-event/student/**"
                        )
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/feedback/submit"
                        )
                        .hasAnyRole("ADMIN", "STUDENT")

                        // -------------------- PAYOUT --------------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/payout"
                        ).hasRole("TEACHER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/payout"
                        ).hasRole("TEACHER")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/payout"
                        ).hasRole("TEACHER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/session/*"
                        )
                        .hasAnyRole("ADMIN", "STUDENT", "TEACHER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/session-event/*"
                        )
                        .hasAnyRole("ADMIN", "STUDENT", "TEACHER")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/report"
                        ).hasRole("STUDENT")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/report"
                        ).hasRole("STUDENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/report"
                        ).hasRole("STUDENT")


                        // -------------------- EVERYTHING ELSE --------------------

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