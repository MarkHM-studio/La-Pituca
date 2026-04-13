package com.restobar.lapituca.controller;

import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.response.*;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.MovimientoInsumoService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovimientoInsumoController.class)
@Import(SecurityConfig.class)
class MovimientoInsumoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovimientoInsumoService movimientoInsumoService;

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

    // =========================
    // 🔹 Helpers
    // =========================

    private MovimientoInsumoListadoResponse mockListado() {
        return new MovimientoInsumoListadoResponse(
                1L,
                LocalDateTime.now(),
                new BigDecimal("10"),
                "KG",
                100L,
                "Harina",
                200L
        );
    }

    private MovimientoInsumoDetalleResponse mockDetalle() {
        return new MovimientoInsumoDetalleResponse(
                1L,
                new BigDecimal("10"),
                "KG",
                LocalDateTime.now(),
                LocalDateTime.now(),
                new InsumoDetalleEnMovimientoResponse(
                        100L,
                        "Harina",
                        "KG",
                        new BigDecimal("50")
                ),
                new ComprobanteResumenEnMovimientoResponse(
                        200L,
                        new BigDecimal("150.00"),
                        "PAGADO",
                        LocalDateTime.now()
                )
        );
    }

    // =========================
    // 🔹 GET /api/movimiento-insumo
    // =========================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listar_deberiaRetornar200() throws Exception {

        when(movimientoInsumoService.listar())
                .thenReturn(List.of(mockListado()));

        mockMvc.perform(get("/api/movimiento-insumo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].insumoNombre").value("Harina"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void listar_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {

        mockMvc.perform(get("/api/movimiento-insumo"))
                .andExpect(status().isForbidden());

        verify(movimientoInsumoService, never()).listar();
    }

    @Test
    void listar_deberiaRetornar401_cuandoNoAutenticado() throws Exception {

        mockMvc.perform(get("/api/movimiento-insumo"))
                .andExpect(status().isUnauthorized());

        verify(movimientoInsumoService, never()).listar();
    }

    // =========================
    // 🔹 GET /api/movimiento-insumo/{id}
    // =========================

    @Test
    @WithMockUser(roles = "CAJERO")
    void obtenerDetalle_deberiaRetornar200() throws Exception {

        when(movimientoInsumoService.obtenerDetalle(1L))
                .thenReturn(mockDetalle());

        mockMvc.perform(get("/api/movimiento-insumo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.insumo.nombre").value("Harina"))
                .andExpect(jsonPath("$.comprobante.estado").value("PAGADO"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void obtenerDetalle_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {

        mockMvc.perform(get("/api/movimiento-insumo/1"))
                .andExpect(status().isForbidden());

        verify(movimientoInsumoService, never()).obtenerDetalle(any());
    }

    @Test
    void obtenerDetalle_deberiaRetornar401_cuandoNoAutenticado() throws Exception {

        mockMvc.perform(get("/api/movimiento-insumo/1"))
                .andExpect(status().isUnauthorized());

        verify(movimientoInsumoService, never()).obtenerDetalle(any());
    }
}