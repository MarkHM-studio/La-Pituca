package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.TipoEntregaRequest;
import com.restobar.lapituca.dto.response.TipoEntregaResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.TipoEntregaService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TipoEntregaController.class)
@Import(SecurityConfig.class)
class TipoEntregaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TipoEntregaService tipoEntregaService;

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
    @WithMockUser(roles = "ADMINISTRADOR")
    void crear_deberiaRetornar201() throws Exception {
        when(tipoEntregaService.guardar(any())).thenReturn(tipoEntregaResponse(1L));

        mockMvc.perform(post("/api/tipoEntrega")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TipoEntregaRequest("Delivery"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tipoEntrega/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        mockMvc.perform(post("/api/tipoEntrega")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TipoEntregaRequest("abc"))))
                .andExpect(status().isBadRequest());

        verify(tipoEntregaService, never()).guardar(any());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/tipoEntrega")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TipoEntregaRequest("Delivery"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/tipoEntrega")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TipoEntregaRequest("Delivery"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200() throws Exception {
        when(tipoEntregaService.listarTodos()).thenReturn(List.of(tipoEntregaResponse(2L)));

        mockMvc.perform(get("/api/tipoEntrega"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar200() throws Exception {
        when(tipoEntregaService.obtenerPorId(3L)).thenReturn(tipoEntregaResponse(3L));

        mockMvc.perform(get("/api/tipoEntrega/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(get("/api/tipoEntrega/0"))
                .andExpect(status().isBadRequest());

        verify(tipoEntregaService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar200() throws Exception {
        when(tipoEntregaService.actualizar(eq(4L), any())).thenReturn(tipoEntregaResponse(4L));

        mockMvc.perform(put("/api/tipoEntrega/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TipoEntregaRequest("Para Llevar"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        mockMvc.perform(put("/api/tipoEntrega/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TipoEntregaRequest("M"))))
                .andExpect(status().isBadRequest());

        verify(tipoEntregaService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminar_deberiaRetornar204() throws Exception {
        doNothing().when(tipoEntregaService).eliminar(5L);

        mockMvc.perform(delete("/api/tipoEntrega/5"))
                .andExpect(status().isNoContent());

        verify(tipoEntregaService).eliminar(5L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminar_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(delete("/api/tipoEntrega/-1"))
                .andExpect(status().isBadRequest());

        verify(tipoEntregaService, never()).eliminar(any());
    }

    private static TipoEntregaResponse tipoEntregaResponse(Long id) {
        return new TipoEntregaResponse(
                id,
                "Tipo " + id,
                LocalDateTime.of(2026, 1, 10, 8, 0),
                LocalDateTime.of(2026, 1, 10, 8, 30)
        );
    }
}