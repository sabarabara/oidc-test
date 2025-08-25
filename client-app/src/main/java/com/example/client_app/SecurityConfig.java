package com.example.client_app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private LoginSuccessAuditHandler successHandler;

    @Autowired
    private LoginFailureAuditHandler failureHandler;

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/index", "/error").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login();
    return http.build();
}

}

/*
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private LoginSuccessAuditHandler successHandler;

    @Autowired
    private LoginFailureAuditHandler failureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            );
        return http.build();
    }
}
 */
