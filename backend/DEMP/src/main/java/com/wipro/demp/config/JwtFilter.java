package com.wipro.demp.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
 

 
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
@Component
public class JwtFilter extends OncePerRequestFilter {
 
    @Autowired
    private JwtUtil jwtUtil;
 
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
 
        String path = request.getRequestURI();
       
        if (
            path.startsWith("/api/auth/register") ||
            path.startsWith("/api/auth/login")
        ) {
            filterChain.doFilter(request, response);
            return;
        }
 
        String authHeader = request.getHeader("Authorization");
 
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);
                String authority = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role;
                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(authority));

                if (email != null) {
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("JWT Verified: User=" + email + ", Role=" + authority);
                } else {
                    System.err.println("Invalid JWT Token: Could not extract email.");
                }
            } else {
                System.err.println("Missing or Malformed Authorization Header.");
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT Token has expired.");
            return;
        } catch (io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.MalformedJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT Token.");
            return;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT Authentication failed.");
            return;
        }
 
        filterChain.doFilter(request, response);
    }
}