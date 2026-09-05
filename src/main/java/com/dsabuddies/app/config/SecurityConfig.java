package com.dsabuddies.app.config;

import com.dsabuddies.app.security.CustomOAuth2UserService;
import com.dsabuddies.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserService userService;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                List<String> origins = Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty() && !s.equals("*"))
                        .toList();
                config.setAllowedOrigins(origins.isEmpty() ? List.of("http://localhost:5173") : origins);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(List.of("Content-Type", "Authorization"));
                config.setAllowCredentials(true);
                return config;
            }))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login/**", "/error", "/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.ico", "/api/auth/status").permitAll()
                .requestMatchers("/api/tasks/*/complete", "/api/tasks/**/complete").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/task-sheets", "/api/task-sheets/**", "/api/tasks", "/api/tasks/**", "/api/topics", "/api/topics/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/task-sheets", "/api/task-sheets/**", "/api/tasks", "/api/tasks/**", "/api/topics", "/api/topics/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/task-sheets", "/api/task-sheets/**", "/api/tasks", "/api/tasks/**", "/api/topics", "/api/topics/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll() // Forward everything else to SPA
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler((request, response, authentication) -> {
                    if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
                        userService.getOrCreateUser(oAuth2User);
                    }
                    response.sendRedirect("/dashboard");
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny));

        return http.build();
    }
}
