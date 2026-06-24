package com.luminar.api.config;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAuthorizationFilterTests {

    private final RoleAuthorizationFilter filter = new RoleAuthorizationFilter();

    @Test
    void permiteConsultasSinRol() throws ServletException, IOException {
        FilterResult result = execute("GET", "/api/productos", null);

        assertThat(result.response().getStatus()).isEqualTo(200);
        assertThat(result.chain().getRequest()).isNotNull();
    }

    @Test
    void permiteAlAdminModificarCualquierRecurso() throws ServletException, IOException {
        FilterResult result = execute("DELETE", "/api/clientes/7", "admin");

        assertThat(result.response().getStatus()).isEqualTo(200);
        assertThat(result.chain().getRequest()).isNotNull();
    }

    @Test
    void permiteAlSupervisorModificarFacturas() throws ServletException, IOException {
        FilterResult result = execute("POST", "/api/facturas", "supervisor");

        assertThat(result.response().getStatus()).isEqualTo(200);
        assertThat(result.chain().getRequest()).isNotNull();
    }

    @Test
    void impideAlSupervisorModificarOtrosRecursos() throws ServletException, IOException {
        FilterResult result = execute("PUT", "/api/productos/3", "supervisor");

        assertThat(result.response().getStatus()).isEqualTo(403);
        assertThat(result.chain().getRequest()).isNull();
    }

    @Test
    void impideAlEmpleadoRealizarMutaciones() throws ServletException, IOException {
        FilterResult result = execute("DELETE", "/api/facturas/5", "empleado");

        assertThat(result.response().getStatus()).isEqualTo(403);
        assertThat(result.chain().getRequest()).isNull();
    }

    @Test
    void rechazaMutacionesSinRol() throws ServletException, IOException {
        FilterResult result = execute("POST", "/api/clientes", null);

        assertThat(result.response().getStatus()).isEqualTo(403);
        assertThat(result.chain().getRequest()).isNull();
    }

    @Test
    void conservaPublicosLosEndpointsDeAutenticacion() throws ServletException, IOException {
        FilterResult result = execute("POST", "/api/auth/login", null);

        assertThat(result.response().getStatus()).isEqualTo(200);
        assertThat(result.chain().getRequest()).isNotNull();
    }

    private FilterResult execute(String method, String path, String role)
        throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        if (role != null) {
            request.addHeader(RoleAuthorizationFilter.ROLE_HEADER, role);
        }

        filter.doFilter(request, response, chain);
        return new FilterResult(response, chain);
    }

    private record FilterResult(
        MockHttpServletResponse response,
        MockFilterChain chain
    ) {
    }
}
