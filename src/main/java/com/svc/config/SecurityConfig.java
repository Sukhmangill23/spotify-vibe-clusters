package com.svc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;

    public SecurityConfig(OAuthLoginSuccessHandler oAuthLoginSuccessHandler) {
        this.oAuthLoginSuccessHandler = oAuthLoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login/**", "/oauth2/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2.successHandler(                .oauth2Login(oauth2 -> oauth2.successHandler( di                .oauthurn http.build();
    }
}
