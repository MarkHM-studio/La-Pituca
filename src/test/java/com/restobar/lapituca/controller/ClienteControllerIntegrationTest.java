package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.ClienteRequest;
import com.restobar.lapituca.dto.response.ClienteResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.ClienteService;
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

@WebMvcTest(ClienteController.class)
@Import(SecurityConfig.class)
class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

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
        when(clienteService.guardar(any())).thenReturn(clienteResponse(1L));

        mockMvc.perform(post("/api/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/cliente/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crear_deberiaRetornar400_cuandoRequestInvalido() throws Exception {
        ClienteRequest invalido = new ClienteRequest("", "A", null, "123", "correo-invalido", "", null);

        mockMvc.perform(post("/api/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).guardar(any());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200_conFiltroEstado() throws Exception {
        when(clienteService.listarTodos("ACTIVO")).thenReturn(List.of(clienteResponse(2L)));

        mockMvc.perform(get("/api/cliente").param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));

        verify(clienteService).listarTodos("ACTIVO");
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void listarTodos_deberiaRetornar200_sinFiltro() throws Exception {
        when(clienteService.listarTodos(null)).thenReturn(List.of(clienteResponse(3L)));

        mockMvc.perform(get("/api/cliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));

        verify(clienteService).listarTodos(null);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void listarTodos_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(get("/api/cliente"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void obtenerPorId_deberiaRetornar200() throws Exception {
        when(clienteService.obtenerPorId(4L)).thenReturn(clienteResponse(4L));

        mockMvc.perform(get("/api/cliente/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void obtenerPorId_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(get("/api/cliente/0"))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void actualizar_deberiaRetornar200() throws Exception {
        when(clienteService.actualizar(eq(5L), any())).thenReturn(clienteResponse(5L));

        mockMvc.perform(put("/api/cliente/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void actualizar_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        ClienteRequest invalido = new ClienteRequest("", "A", null, "123", "correo-invalido", "", null);

        mockMvc.perform(put("/api/cliente/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void actualizar_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(put("/api/cliente/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminar_deberiaRetornar204() throws Exception {
        doNothing().when(clienteService).eliminar(6L);

        mockMvc.perform(delete("/api/cliente/6"))
                .andExpect(status().isNoContent());

        verify(clienteService).eliminar(6L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminar_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(delete("/api/cliente/-1"))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).eliminar(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void activar_deberiaRetornar204() throws Exception {
        doNothing().when(clienteService).activar(7L);

        mockMvc.perform(patch("/api/cliente/7/activar"))
                .andExpect(status().isNoContent());

        verify(clienteService).activar(7L);
    }

    @Test
    @WithMockUser(roles = "RECEPCIONISTA")
    void activar_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(patch("/api/cliente/7/activar"))
                .andExpect(status().isForbidden());
    }

    private static ClienteRequest clienteRequestValido() {
        return new ClienteRequest(
                "Juan",
                "Perez",
                LocalDate.of(1998, 5, 20),
                "999888777",
                "juan.perez@mail.com",
                "Lima",
                1L
        );
    }

    private static ClienteResponse clienteResponse(Long id) {
        return new ClienteResponse(
                id,
                "Juan",
                "Perez",
                LocalDate.of(1998, 5, 20),
                "999888777",
                "juan.perez@mail.com",
                "ACTIVO",
                "REGULAR",
                "Lima",
                LocalDateTime.of(2026, 1, 10, 12, 0),
                LocalDateTime.of(2026, 1, 10, 12, 30),
                1L,
                "juanp"
        );
    }
}
