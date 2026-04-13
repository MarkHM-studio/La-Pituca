package com.restobar.lapituca.controller;

import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.response.TransaccionResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.TransaccionService;
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

@WebMvcTest(TransaccionController.class)
@Import(SecurityConfig.class)
class TransaccionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransaccionService transaccionService;

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

    // Helper para crear mock limpio
    private TransaccionResponse mockTransaccion() {
        return new TransaccionResponse(
                1L,
                "mp-123",
                "pref-123",
                "ext-123",
                "COMPLETADO",
                "approved",
                "accredited",
                new BigDecimal("100.50"),
                LocalDateTime.now(),
                LocalDateTime.now(),
                10L,
                20L
        );
    }

    // =========================
    // GET /transacciones
    // =========================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200() throws Exception {

        when(transaccionService.listarTodos())
                .thenReturn(List.of(mockTransaccion()));

        mockMvc.perform(get("/transacciones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].estado").value("COMPLETADO"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void listarTodos_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {

        mockMvc.perform(get("/transacciones"))
                .andExpect(status().isForbidden());

        verify(transaccionService, never()).listarTodos();
    }

    @Test
    void listarTodos_deberiaRetornar401_cuandoNoAutenticado() throws Exception {

        mockMvc.perform(get("/transacciones"))
                .andExpect(status().isUnauthorized());

        verify(transaccionService, never()).listarTodos();
    }

    // =========================
    // GET /transacciones/{id}
    // =========================

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void obtenerPorId_deberiaRetornar200() throws Exception {

        when(transaccionService.obtenerPorId(1L))
                .thenReturn(mockTransaccion());

        mockMvc.perform(get("/transacciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void obtenerPorId_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {

        mockMvc.perform(get("/transacciones/1"))
                .andExpect(status().isForbidden());

        verify(transaccionService, never()).obtenerPorId(any());
    }

    @Test
    void obtenerPorId_deberiaRetornar401_cuandoNoAutenticado() throws Exception {

        mockMvc.perform(get("/transacciones/1"))
                .andExpect(status().isUnauthorized());

        verify(transaccionService, never()).obtenerPorId(any());
    }
}