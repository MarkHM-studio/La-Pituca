package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.RecetaRequest;
import com.restobar.lapituca.dto.response.RecetaResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.RecetaService;
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

@WebMvcTest(RecetaController.class)
@Import(SecurityConfig.class)
class RecetaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecetaService recetaService;

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
        when(recetaService.crear(any())).thenReturn(List.of(recetaResponse(1L, 10L)));

        mockMvc.perform(post("/api/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recetaRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/receta/producto/10"))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void crear_deberiaRetornar201_paraAlmacenero() throws Exception {
        when(recetaService.crear(any())).thenReturn(List.of(recetaResponse(2L, 10L)));

        mockMvc.perform(post("/api/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recetaRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        RecetaRequest invalido = new RecetaRequest(0L, List.of(), List.of(), List.of());

        mockMvc.perform(post("/api/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(recetaService, never()).crear(any());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recetaRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/receta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recetaRequestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void actualizar_deberiaRetornar200() throws Exception {
        when(recetaService.actualizar(eq(10L), any())).thenReturn(List.of(recetaResponse(3L, 10L)));

        mockMvc.perform(put("/api/receta/producto/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recetaRequestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void actualizar_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        RecetaRequest invalido = new RecetaRequest(0L, List.of(), List.of(), List.of());

        mockMvc.perform(put("/api/receta/producto/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(recetaService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void actualizar_deberiaRetornar400_cuandoProductoIdInvalido() throws Exception {
        mockMvc.perform(put("/api/receta/producto/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recetaRequestValido())))
                .andExpect(status().isBadRequest());

        verify(recetaService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void actualizar_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(put("/api/receta/producto/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recetaRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200() throws Exception {
        when(recetaService.listarTodos()).thenReturn(List.of(recetaResponse(4L, 10L)));

        mockMvc.perform(get("/api/receta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4));
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void listarTodos_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(get("/api/receta"))
                .andExpect(status().isForbidden());
    }

    private static RecetaRequest recetaRequestValido() {
        return new RecetaRequest(
                10L,
                List.of(1L, 2L),
                List.of(BigDecimal.valueOf(1.5), BigDecimal.valueOf(2.0)),
                List.of("KG", "LT")
        );
    }

    private static RecetaResponse recetaResponse(Long id, Long productoId) {
        return new RecetaResponse(
                id,
                productoId,
                "Producto " + productoId,
                1L,
                "Insumo 1",
                BigDecimal.valueOf(1.5),
                "KG"
        );
    }
}