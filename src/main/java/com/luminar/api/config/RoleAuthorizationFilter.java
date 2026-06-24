package com.luminar.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Component
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    public static final String ROLE_HEADER = "X-User-Role";

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final Set<String> VALID_ROLES = Set.of("admin", "supervisor", "empleado");
    private static final String AUTH_PATTERN = "/api/auth/**";
    private static final String FACTURAS_PATTERN = "/api/facturas/**";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String method = request.getMethod().toUpperCase(Locale.ROOT);

        if (
            SAFE_METHODS.contains(method)
                || pathMatcher.match(AUTH_PATTERN, request.getRequestURI())
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String role = normalizeRole(request.getHeader(ROLE_HEADER));
        if (!VALID_ROLES.contains(role)) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Falta un rol de usuario valido."
            );
            return;
        }

        boolean isAdmin = "admin".equals(role);
        boolean isSupervisorManagingInvoices =
            "supervisor".equals(role)
                && pathMatcher.match(FACTURAS_PATTERN, request.getRequestURI());

        if (!isAdmin && !isSupervisorManagingInvoices) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "El rol no tiene permiso para realizar esta operacion."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
    }
}
