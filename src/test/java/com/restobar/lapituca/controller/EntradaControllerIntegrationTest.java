package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.EntradaRequest;
import com.restobar.lapituca.dto.response.EntradaResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.EntradaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EntradaController.class)
@Import(SecurityConfig.class)
class EntradaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntradaService entradaService;

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

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void crear_deberiaRetornar201() throws Exception {
        when(entradaService.crear(any())).thenReturn(entradaResponse(1L));

        mockMvc.perform(post("/api/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entradaRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/entrada/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        EntradaRequest invalido = new EntradaRequest(1L, null, BigDecimal.valueOf(-1), "", BigDecimal.ZERO, 0L, null);

        mockMvc.perform(post("/api/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(entradaService, never()).crear(any());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entradaRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entradaRequestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200() throws Exception {
        when(entradaService.listarTodos()).thenReturn(List.of(entradaResponse(2L)));

        mockMvc.perform(get("/api/entrada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void listarTodos_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(get("/api/entrada"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void actualizar_deberiaRetornar200() throws Exception {
        when(entradaService.actualizar(eq(3L), any())).thenReturn(entradaResponse(3L));

        mockMvc.perform(put("/api/entrada/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entradaRequestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void actualizar_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        EntradaRequest invalido = new EntradaRequest(1L, null, BigDecimal.valueOf(-1), "", BigDecimal.ZERO, 0L, null);

        mockMvc.perform(put("/api/entrada/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(entradaService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void actualizar_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(put("/api/entrada/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entradaRequestValido())))
                .andExpect(status().isForbidden());
    }

    private static EntradaRequest entradaRequestValido() {
        return new EntradaRequest(
                1L,
                null,
                BigDecimal.valueOf(10),
                "KG",
                BigDecimal.valueOf(12.50),
                1L,
                1L
        );
    }

    private static EntradaResponse entradaResponse(Long id) {
        return new EntradaResponse(
                id,
                1L,
                null,
                BigDecimal.valueOf(10),
                "KG",
                BigDecimal.valueOf(12.50),
                BigDecimal.valueOf(125),
                1L,
                1L,
                LocalDateTime.of(2026, 1, 10, 8, 0)
        );
    }
}