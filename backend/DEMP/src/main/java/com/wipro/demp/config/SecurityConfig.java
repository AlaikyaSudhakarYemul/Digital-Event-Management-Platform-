package com.wipro.demp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.wipro.demp.constants.DempConstants;

import java.util.List;
 
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
 
    @Autowired
    private JwtFilter jwtFilter;
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    DempConstants.API_URL + DempConstants.REGISTER_URL,
                    DempConstants.API_URL + DempConstants.LOGIN_URL,
                    DempConstants.API_URL + "/auth/**",
                    "/error"
                ).permitAll()
                                .requestMatchers(DempConstants.API_URL + DempConstants.USER_URL + "/all", DempConstants.API_URL + DempConstants.USER_URL + "/organizers").hasRole("ADMIN")
                                .requestMatchers(DempConstants.API_URL + DempConstants.USER_URL + "/events/**").hasRole("ORGANIZER")
                .requestMatchers(DempConstants.API_URL + DempConstants.USER_URL + "/**").authenticated()
                                .requestMatchers(DempConstants.API_URL + DempConstants.CHATBOT_URL + "/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/admin/**").permitAll()
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, DempConstants.API_URL + DempConstants.SPEAKERS_URL + "/**").permitAll()
                                .requestMatchers(DempConstants.API_URL + DempConstants.SPEAKERS_URL + "/**").hasRole("ADMIN")
                                .requestMatchers(DempConstants.API_URL + DempConstants.EVENTS_URL + "/**").permitAll()
                                .requestMatchers(HttpMethod.POST,DempConstants.API_URL + DempConstants.REGISTRATIONS_URL + "/**").hasRole("USER")
                                .requestMatchers(HttpMethod.POST, DempConstants.API_URL + "/payments/orders").hasRole("USER")
                                .requestMatchers(HttpMethod.POST, DempConstants.API_URL + "/payments/verify").hasRole("USER")
                                .requestMatchers(HttpMethod.POST, DempConstants.API_URL + "/payments/pending").hasRole("USER")
                                .requestMatchers(DempConstants.API_URL + DempConstants.REGISTRATIONS_URL + "/event/{eventId}").hasRole("ORGANIZER")
                                .requestMatchers(DempConstants.API_URL + DempConstants.REGISTRATIONS_URL + "/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
 
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(DempConstants.FRONTEND_URL));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}