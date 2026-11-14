package com.university.ums.security;

import com.university.ums.model.UserAccount;
import com.university.ums.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserRepository repo) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // JSON API; kept simple for the lab
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/index.html", "/error", "/api/auth/login").permitAll()
                    .requestMatchers("/api/admins/**").hasRole("ADMIN")
                .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "STUDENT")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((req, res, auth) -> {
                    String roles = auth.getAuthorities().stream()
                            .map(a -> a.getAuthority().replace("ROLE_", ""))
                            .collect(Collectors.joining(","));
                    res.setStatus(HttpStatus.OK.value());
                    res.setContentType("application/json");
                    Long id = repo.findByUsername(auth.getName()).map(UserAccount::getId).orElse(null);
                    res.getWriter().write("{\"message\":\"Login successful\",\"id\":" + id
                            + ",\"username\":\"" + auth.getName() + "\",\"role\":\"" + roles + "\"}");
                })
                .failureHandler((req, res, ex) -> {
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType("application/json");
                    res.getWriter().write("{\"message\":\"Invalid username or password\"}");
                }))
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) -> {
                    res.setStatus(HttpStatus.OK.value());
                    res.setContentType("application/json");
                    res.getWriter().write("{\"message\":\"Logged out\"}");
                })
                .deleteCookies("JSESSIONID"))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }
}
