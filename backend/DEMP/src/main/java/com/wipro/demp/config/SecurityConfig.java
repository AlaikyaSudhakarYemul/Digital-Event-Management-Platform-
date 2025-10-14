package com.wipro.demp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 
import static org.springframework.security.config.Customizer.withDefaults;
 
@Configuration
@EnableWebSecurity
public class SecurityConfig {
 
    @Autowired
    private JwtFilter jwtFilter;
 
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/**",
                    "/error"
                ).permitAll()
                .requestMatchers("/api/user/**").authenticated()
                // .requestMatchers(HttpMethod.GET,"/api/events/**").permitAll()
                //                 .requestMatchers("/api/events/**").hasRole("ORGANIZER")
                                .requestMatchers(HttpMethod.GET, "/api/admin/**").permitAll()
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/speakers/**").permitAll()
                                .requestMatchers("/api/speakers/**").hasRole("ADMIN")
//                                .requestMatchers(HttpMethod.POST,"/api/registrations/**").hasRole("USER")
//                                .requestMatchers("/api/registrations/event/{eventId}").hasRole("ORGANIZER")
//                                .requestMatchers("/api/registrations/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
 
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}