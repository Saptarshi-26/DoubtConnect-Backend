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
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()


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
                        .hasAnyRole("ADMIN","TEACHER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/teacher/**")
                        .hasAnyRole("ADMIN","TEACHER")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/teacher/**")
                        .hasAnyRole("ADMIN","TEACHER")

                        .requestMatchers("/feedback/rate/**")
                        .hasAnyRole("ADMIN","STUDENT")

                        .requestMatchers("/user/**")
                        .hasRole("ADMIN")

                        // Student endpoints
                        .requestMatchers("/student/**")
                        .hasAnyRole("ADMIN","STUDENT")

                        .requestMatchers("/student/all/**")
                        .hasRole("ADMIN")


                        .requestMatchers(HttpMethod.POST,"/session/save/**")
                        .hasAnyRole("ADMIN","STUDENT")

                        .requestMatchers("/session/payment/**")
                        .hasAnyRole("ADMIN","STUDENT")

                        .requestMatchers(HttpMethod.PUT,"/session/update/**")
                        .hasAnyRole("ADMIN","STUDENT")

                        .requestMatchers(HttpMethod.DELETE,"/session/delete/**")
                        .hasAnyRole("ADMIN","STUDENT")

                        .requestMatchers(HttpMethod.POST,"/session/accept/**")
                        .hasAnyRole("ADMIN","TEACHER")

                        .requestMatchers(HttpMethod.POST,"/session/reject/**")
                        .hasAnyRole("ADMIN","TEACHER")

                        .requestMatchers("/payment/order/**")
                        .hasAnyRole("ADMIN","STUDENT")

                        .requestMatchers("/oauth/google/**")
                        .permitAll()



                                // Feedback
                                .requestMatchers(HttpMethod.POST, "/feedback/rate/**")
                                .hasAnyRole("ADMIN", "STUDENT")

                                .requestMatchers(HttpMethod.POST, "/feedback/review/**")
                                .hasAnyRole("ADMIN", "STUDENT")

                                .requestMatchers(HttpMethod.GET, "/feedback/**")
                                .permitAll()

// Session Events
                                .requestMatchers(HttpMethod.GET, "/session-event/student/**")
                                .hasAnyRole("ADMIN", "STUDENT")

                                .requestMatchers(HttpMethod.GET, "/session-event/teacher/**")
                                .hasAnyRole("ADMIN", "TEACHER")


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