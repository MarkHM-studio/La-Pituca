package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.MarcaRequest;
import com.restobar.lapituca.dto.response.MarcaResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.MarcaService;
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

@WebMvcTest(MarcaController.class)
@Import(SecurityConfig.class)
class MarcaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MarcaService marcaService;

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
        when(marcaService.guardar(any())).thenReturn(marcaResponse(1L));

        mockMvc.perform(post("/api/marca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarcaRequest("Coca Cola"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/marca/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void crear_deberiaRetornar201_paraAlmacenero() throws Exception {
        when(marcaService.guardar(any())).thenReturn(marcaResponse(2L));

        mockMvc.perform(post("/api/marca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarcaRequest("Pepsi Max"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        mockMvc.perform(post("/api/marca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarcaRequest("A1"))))
                .andExpect(status().isBadRequest());

        verify(marcaService, never()).guardar(any());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/marca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarcaRequest("Coca Cola"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/marca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarcaRequest("Coca Cola"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void listarTodos_deberiaRetornar200() throws Exception {
        when(marcaService.listarTodos()).thenReturn(List.of(marcaResponse(3L)));

        mockMvc.perform(get("/api/marca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void listarTodos_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(get("/api/marca"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar200() throws Exception {
        when(marcaService.obtenerPorId(4L)).thenReturn(marcaResponse(4L));

        mockMvc.perform(get("/api/marca/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(get("/api/marca/0"))
                .andExpect(status().isBadRequest());

        verify(marcaService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void actualizar_deberiaRetornar200() throws Exception {
        when(marcaService.actualizar(eq(5L), any())).thenReturn(marcaResponse(5L));

        mockMvc.perform(put("/api/marca/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarcaRequest("Inca Kola"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void actualizar_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        mockMvc.perform(put("/api/marca/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MarcaRequest("M"))))
                .andExpect(status().isBadRequest());

        verify(marcaService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void eliminar_deberiaRetornar204() throws Exception {
        doNothing().when(marcaService).eliminar(6L);

        mockMvc.perform(delete("/api/marca/6"))
                .andExpect(status().isNoContent());

        verify(marcaService).eliminar(6L);
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void eliminar_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(delete("/api/marca/-1"))
                .andExpect(status().isBadRequest());

        verify(marcaService, never()).eliminar(any());
    }

    private static MarcaResponse marcaResponse(Long id) {
        return new MarcaResponse(
                id,
                "Marca " + id,
                LocalDateTime.of(2026, 1, 10, 8, 0),
                LocalDateTime.of(2026, 1, 10, 8, 30)
        );
    }
}