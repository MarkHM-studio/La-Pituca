package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.ReservaRequest;
import com.restobar.lapituca.dto.response.MesasDisponiblesResponse;
import com.restobar.lapituca.dto.response.ReservaResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.ReservaService;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
@Import(SecurityConfig.class)
class ReservaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservaService reservaService;

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
    @WithMockUser(username = "cliente@mail.com", roles = "CLIENTE")
    void crear_deberiaRetornar201() throws Exception {
        when(reservaService.crear(any())).thenReturn(reservaResponse(1L));

        mockMvc.perform(post("/api/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/reserva/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "cliente@mail.com", roles = "CLIENTE")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        ReservaRequest invalido = new ReservaRequest(LocalDate.now().minusDays(1), null, 0, null, Set.of(), null);

        mockMvc.perform(post("/api/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).crear(any());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void actualizar_deberiaRetornar200() throws Exception {
        when(reservaService.actualizar(eq(2L), any())).thenReturn(reservaResponse(2L));

        mockMvc.perform(put("/api/reserva/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaRequestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(username = "cliente@mail.com", roles = "CLIENTE")
    void listar_deberiaUsarListarPorUsername_cuandoEsCliente() throws Exception {
        when(reservaService.listarPorUsername("cliente@mail.com")).thenReturn(List.of(reservaResponse(3L)));

        mockMvc.perform(get("/api/reserva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));

        verify(reservaService).listarPorUsername("cliente@mail.com");
        verify(reservaService, never()).listar();
    }

    @Test
    @WithMockUser(username = "recep@mail.com", roles = "RECEPCIONISTA")
    void listar_deberiaUsarListarGeneral_cuandoEsRecepcionista() throws Exception {
        when(reservaService.listar()).thenReturn(List.of(reservaResponse(4L)));

        mockMvc.perform(get("/api/reserva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4));

        verify(reservaService).listar();
        verify(reservaService, never()).listarPorUsername(any());
    }

    @Test
    @WithMockUser(username = "cliente@mail.com", roles = "CLIENTE")
    void obtenerPorId_deberiaUsarMetodoCliente() throws Exception {
        when(reservaService.obtenerPorIdParaUsername(5L, "cliente@mail.com")).thenReturn(reservaResponse(5L));

        mockMvc.perform(get("/api/reserva/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(reservaService).obtenerPorIdParaUsername(5L, "cliente@mail.com");
        verify(reservaService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(username = "recep@mail.com", roles = "RECEPCIONISTA")
    void obtenerPorId_deberiaUsarMetodoGeneral() throws Exception {
        when(reservaService.obtenerPorId(6L)).thenReturn(reservaResponse(6L));

        mockMvc.perform(get("/api/reserva/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6));

        verify(reservaService).obtenerPorId(6L);
        verify(reservaService, never()).obtenerPorIdParaUsername(any(), any());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void verMesasDisponibles_deberiaRetornar200() throws Exception {
        when(reservaService.verMesasDisponibles(LocalDate.of(2026, 5, 20), LocalTime.of(19, 30)))
                .thenReturn(List.of(new MesasDisponiblesResponse(10L, "MESA-10", false)));

        mockMvc.perform(get("/api/reserva/mesas-disponibles")
                        .param("fecha", "2026-05-20")
                        .param("hora", "19:30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mesaId").value(10));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void verMesasDisponibles_deberiaRetornar400_cuandoFormatoHoraInvalido() throws Exception {
        mockMvc.perform(get("/api/reserva/mesas-disponibles")
                        .param("fecha", "2026-05-20")
                        .param("hora", "7pm"))
                .andExpect(status().isBadRequest());

        verify(reservaService, never()).verMesasDisponibles(any(), any());
    }

    @Test
    @WithMockUser(username = "cliente@mail.com", roles = "CLIENTE")
    void cancelar_deberiaRetornar204_yEsClienteTrue() throws Exception {
        doNothing().when(reservaService).cancelar(7L, "cliente@mail.com", true);

        mockMvc.perform(patch("/api/reserva/7/cancelar"))
                .andExpect(status().isNoContent());

        verify(reservaService).cancelar(7L, "cliente@mail.com", true);
    }

    @Test
    @WithMockUser(username = "recep@mail.com", roles = "RECEPCIONISTA")
    void cancelar_deberiaRetornar204_yEsClienteFalse() throws Exception {
        doNothing().when(reservaService).cancelar(8L, "recep@mail.com", false);

        mockMvc.perform(patch("/api/reserva/8/cancelar"))
                .andExpect(status().isNoContent());

        verify(reservaService).cancelar(8L, "recep@mail.com", false);
    }

    @Test
    @WithMockUser(username = "recep@mail.com", roles = "RECEPCIONISTA")
    void verificarReserva_deberiaRetornar200() throws Exception {
        when(reservaService.verificarReserva(9L, "recep@mail.com")).thenReturn(reservaResponse(9L));

        mockMvc.perform(patch("/api/reserva/9/verificar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void verificarReserva_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(patch("/api/reserva/9/verificar"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/reserva"))
                .andExpect(status().isUnauthorized());
    }

    private static ReservaRequest reservaRequestValido() {
        return new ReservaRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(19, 30),
                4,
                1L,
                Set.of(2L, 3L),
                1L
        );
    }

    private static ReservaResponse reservaResponse(Long id) {
        return new ReservaResponse(
                id,
                LocalDate.of(2026, 5, 20),
                LocalTime.of(19, 30),
                4,
                "PENDIENTE",
                1L,
                1L,
                List.of(2L, 3L),
                null,
                List.of(),
                LocalDateTime.of(2026, 4, 1, 10, 0),
                null,
                null
        );
    }
}