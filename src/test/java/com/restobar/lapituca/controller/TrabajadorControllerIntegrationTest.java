package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.TrabajadorRequest;
import com.restobar.lapituca.dto.response.TrabajadorResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.TrabajadorService;
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

import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrabajadorController.class)
@Import(SecurityConfig.class)
class TrabajadorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrabajadorService trabajadorService;

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
        when(trabajadorService.guardar(any())).thenReturn(trabajadorResponse(1L));

        mockMvc.perform(post("/api/trabajador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trabajadorRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/trabajador/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        TrabajadorRequest invalido = new TrabajadorRequest("Ana", "Li", "12", "123", "correo", null, null, "ACT", null, null, null);

        mockMvc.perform(post("/api/trabajador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(trabajadorService, never()).guardar(any());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/trabajador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trabajadorRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/trabajador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trabajadorRequestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200_conEstado() throws Exception {
        when(trabajadorService.listarTodos("ACTIVO")).thenReturn(List.of(trabajadorResponse(2L)));

        mockMvc.perform(get("/api/trabajador").param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));

        verify(trabajadorService).listarTodos("ACTIVO");
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar200() throws Exception {
        when(trabajadorService.obtenerPorId(3L)).thenReturn(trabajadorResponse(3L));

        mockMvc.perform(get("/api/trabajador/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(get("/api/trabajador/0"))
                .andExpect(status().isBadRequest());

        verify(trabajadorService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar200() throws Exception {
        when(trabajadorService.actualizar(eq(4L), any())).thenReturn(trabajadorResponse(4L));

        mockMvc.perform(put("/api/trabajador/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trabajadorRequestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        TrabajadorRequest invalido = new TrabajadorRequest("Ana", "Li", "12", "123", "correo", null, null, "ACT", null, null, null);

        mockMvc.perform(put("/api/trabajador/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(trabajadorService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminar_deberiaRetornar204() throws Exception {
        doNothing().when(trabajadorService).eliminar(5L);

        mockMvc.perform(delete("/api/trabajador/5"))
                .andExpect(status().isNoContent());

        verify(trabajadorService).eliminar(5L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminar_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(delete("/api/trabajador/-1"))
                .andExpect(status().isBadRequest());

        verify(trabajadorService, never()).eliminar(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void activar_deberiaRetornar204() throws Exception {
        doNothing().when(trabajadorService).activar(6L);

        mockMvc.perform(patch("/api/trabajador/6/activar"))
                .andExpect(status().isNoContent());

        verify(trabajadorService).activar(6L);
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void activar_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(patch("/api/trabajador/6/activar"))
                .andExpect(status().isForbidden());
    }

    private static TrabajadorRequest trabajadorRequestValido() {
        return new TrabajadorRequest(
                "Carlos",
                "Ramirez",
                "12345678",
                "999888777",
                "carlos.ramirez@mail.com",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                "ACTIVO",
                1L,
                1L,
                1L
        );
    }

    private static TrabajadorResponse trabajadorResponse(Long id) {
        return new TrabajadorResponse(
                id,
                "Carlos",
                "Ramirez",
                "12345678",
                "999888777",
                "carlos.ramirez@mail.com",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                "ACTIVO",
                LocalDateTime.of(2026, 1, 10, 8, 0),
                LocalDateTime.of(2026, 1, 10, 9, 0),
                1L,
                "carlosr",
                2L,
                "JORNADA COMPLETA",
                3L,
                "MAÑANA"
        );
    }
}