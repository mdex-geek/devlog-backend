package com.deepanshu.devlog.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.deepanshu.devlog.Entity.User;
import com.deepanshu.devlog.repository.UserRepository;
import com.deepanshu.devlog.service.JwtService;
import com.deepanshu.devlog.utils.AccountStatus;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/verify-otp") ||
                path.startsWith("/api/v1/auth/refresh")) {

            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");

            // If no Authorization header, just continue with the filter chain
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.debug("No Authorization header found, continuing filter chain");
                filterChain.doFilter(request, response);
                return;
            }

            logger.debug("Processing JWT token for request: {} {}", request.getMethod(), request.getRequestURI());

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null && jwtService.isTokenValid(token, user.getUsername())) {

                    if (user.getStatus() != AccountStatus.ACTIVE) {
                        logger.warn("User {} is not active, status: {}", username, user.getStatus());
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("User account is not active: " + user.getStatus());
                        return;
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user.getUsername(), null, null);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set for user: {}", username);
                } else {
                    logger.debug("Invalid token for user: {}", username);
                }
            }
        } catch (Exception e) {
            // Log the exception but don't block the request
            logger.error("Error processing JWT token", e);
        }

        filterChain.doFilter(request, response);
    }
}
