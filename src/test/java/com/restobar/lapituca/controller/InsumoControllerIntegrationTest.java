package com.restobar.lapituca.controller;

import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.response.InsumoResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.InsumoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InsumoController.class)
@Import(SecurityConfig.class)
class InsumoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InsumoService insumoService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private OAuth2FailureHandler oAuth2FailureHandler;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // 🔹 Helper
    private InsumoResponse mockResponse() {
        return new InsumoResponse(
                1L,
                "Coca Cola",
                new BigDecimal("5.50"),
                new BigDecimal("100"),
                "UNIDAD",
                1L,
                2L
        );
    }

    // =========================
    // 🔹 POST
    // =========================

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void crear_deberiaRetornar201_admin() throws Exception {

        when(insumoService.crear(any())).thenReturn(mockResponse());

        mockMvc.perform(post("/api/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Coca Cola",
                                  "precio": 5.50,
                                  "stock": 100,
                                  "unidadMedida": "UNIDAD",
                                  "marcaId": 1,
                                  "categoriaId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Coca Cola"));
    }

    @Test
    @WithMockUser(roles = {"ALMACENERO"})
    void crear_deberiaRetornar201_almacenero() throws Exception {

        when(insumoService.crear(any())).thenReturn(mockResponse());

        mockMvc.perform(post("/api/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Coca Cola",
                                  "precio": 5.50,
                                  "stock": 100,
                                  "unidadMedida": "UNIDAD",
                                  "marcaId": 1,
                                  "categoriaId": 2
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void crear_deberiaRetornar403() throws Exception {

        mockMvc.perform(post("/api/insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Coca Cola",
                                  "precio": 5.50,
                                  "stock": 100,
                                  "unidadMedida": "UNIDAD",
                                  "marcaId": 1,
                                  "categoriaId": 2
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(insumoService, never()).crear(any());
    }

    // =========================
    // 🔹 GET
    // =========================

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void listarTodos_deberiaRetornar200() throws Exception {

        when(insumoService.listarTodos()).thenReturn(List.of(mockResponse()));

        mockMvc.perform(get("/api/insumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Coca Cola"));
    }

    // =========================
    // 🔹 PUT ALMACENERO
    // =========================

    @Test
    @WithMockUser(roles = {"ALMACENERO"})
    void actualizarRolAlmacenero_deberiaRetornar200() throws Exception {

        when(insumoService.actualizarRolAlmacenero(any(), any()))
                .thenReturn(mockResponse());

        mockMvc.perform(put("/api/insumo/1/almacenero")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Pepsi",
                                  "precio": 6.00,
                                  "stock": 80,
                                  "unidadMedida": "UNIDAD",
                                  "marcaId": 1,
                                  "categoriaId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Coca Cola"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void actualizarRolAlmacenero_deberiaRetornar403() throws Exception {

        mockMvc.perform(put("/api/insumo/1/almacenero")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Pepsi",
                                  "precio": 6.00,
                                  "stock": 80,
                                  "unidadMedida": "UNIDAD",
                                  "marcaId": 1,
                                  "categoriaId": 2
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    // =========================
    // 🔹 PUT ADMIN
    // =========================

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void actualizarRolAdmin_deberiaRetornar200() throws Exception {

        when(insumoService.actualizarRolAdmin(any(), any()))
                .thenReturn(mockResponse());

        mockMvc.perform(put("/api/insumo/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Pepsi",
                                  "precio": 6.00,
                                  "stock": 80,
                                  "unidadMedida": "UNIDAD",
                                  "marcaId": 1,
                                  "categoriaId": 2
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ALMACENERO"})
    void actualizarRolAdmin_deberiaRetornar403() throws Exception {

        mockMvc.perform(put("/api/insumo/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Pepsi",
                                  "precio": 6.00,
                                  "stock": 80,
                                  "unidadMedida": "UNIDAD",
                                  "marcaId": 1,
                                  "categoriaId": 2
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}