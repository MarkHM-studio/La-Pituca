package com.restobar.lapituca.controller;

import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.TipoBilleteraVirtualRequest;
import com.restobar.lapituca.dto.response.TipoBilleteraVirtualResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.TipoBilleteraVirtualService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TipoBilleteraVirtualController.class)
@Import(SecurityConfig.class)
class TipoBilleteraVirtualControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TipoBilleteraVirtualService tipoBilleteraVirtualService;

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
    private TipoBilleteraVirtualResponse mockResponse() {
        return new TipoBilleteraVirtualResponse(
                1L,
                "Yape",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // =========================
    // 🔹 POST /api/billeteraVirtual
    // =========================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crear_deberiaRetornar201() throws Exception {

        when(tipoBilleteraVirtualService.guardar(any()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/billeteraVirtual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Yape"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nombre").value("Yape"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crear_deberiaRetornar403() throws Exception {

        mockMvc.perform(post("/api/billeteraVirtual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Yape"
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(tipoBilleteraVirtualService, never()).guardar(any());
    }

    @Test
    void crear_deberiaRetornar401() throws Exception {

        mockMvc.perform(post("/api/billeteraVirtual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Yape"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // =========================
    // 🔹 GET /api/billeteraVirtual
    // =========================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200() throws Exception {

        when(tipoBilleteraVirtualService.listarTodos())
                .thenReturn(List.of(mockResponse()));

        mockMvc.perform(get("/api/billeteraVirtual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Yape"));
    }

    // =========================
    // 🔹 GET /api/billeteraVirtual/{id}
    // =========================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar200() throws Exception {

        when(tipoBilleteraVirtualService.obtenerPorId(1L))
                .thenReturn(mockResponse());

        mockMvc.perform(get("/api/billeteraVirtual/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar400_cuandoIdInvalido() throws Exception {

        mockMvc.perform(get("/api/billeteraVirtual/0"))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // 🔹 PUT /api/billeteraVirtual/{id}
    // =========================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar200() throws Exception {

        when(tipoBilleteraVirtualService.actualizar(any(), any()))
                .thenReturn(mockResponse());

        mockMvc.perform(put("/api/billeteraVirtual/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Plin"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Yape"));
    }

    // =========================
    // 🔹 DELETE /api/billeteraVirtual/{id}
    // =========================

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminar_deberiaRetornar204() throws Exception {

        mockMvc.perform(delete("/api/billeteraVirtual/1"))
                .andExpect(status().isNoContent());
    }
}