package com.luminar.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/auth/**"
    );

    private final String apiKey;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ApiKeyFilter(@Value("${app.api-key}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno APP_API_KEY.");
        }
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        boolean isPublic = PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isPublic || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader("X-API-Key");
        if (apiKey == null || !apiKey.equals(key)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API Key inválida");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
