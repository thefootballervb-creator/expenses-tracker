package com.fullStack.expenseTracker.security.jwt;

import java.io.IOException;

import com.fullStack.expenseTracker.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


// Login part
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            String requestUri = request.getRequestURI();
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.info("Valid JWT token found for user: {} on path: {}", username, requestUri);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("Loaded user details for: {} with authorities: {}", username, 
                        userDetails.getAuthorities().stream()
                                .map(auth -> auth.getAuthority())
                                .collect(java.util.stream.Collectors.toList()));
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Authentication successful for user: {} on path: {} with roles: {}", 
                        username, requestUri, 
                        userDetails.getAuthorities().stream()
                                .map(auth -> auth.getAuthority())
                                .collect(java.util.stream.Collectors.toList()));
            } else {
                if (jwt == null) {
                    logger.warn("JWT token is null for request: {}", requestUri);
                } else {
                    logger.warn("JWT token validation failed for request: {}", requestUri);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication for request {}: {}", request.getRequestURI(), e.getMessage(), e);
            logger.error("Exception details: ", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}