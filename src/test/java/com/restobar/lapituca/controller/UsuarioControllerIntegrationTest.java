package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.UsuarioRequest;
import com.restobar.lapituca.dto.response.UsuarioResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.UsuarioService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

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
        when(usuarioService.guardar(any())).thenReturn(usuarioResponse(1L));

        mockMvc.perform(post("/api/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/usuario/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        UsuarioRequest invalido = new UsuarioRequest("abc", "123", null);

        mockMvc.perform(post("/api/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).guardar(any());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200_conFiltroEstado() throws Exception {
        when(usuarioService.listarTodos("ACTIVO")).thenReturn(List.of(usuarioResponse(2L)));

        mockMvc.perform(get("/api/usuario").param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));

        verify(usuarioService).listarTodos("ACTIVO");
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar200() throws Exception {
        when(usuarioService.obtenerPorId(3L)).thenReturn(usuarioResponse(3L));

        mockMvc.perform(get("/api/usuario/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerPorId_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(get("/api/usuario/0"))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar200() throws Exception {
        when(usuarioService.actualizar(eq(4L), any())).thenReturn(usuarioResponse(4L));

        mockMvc.perform(put("/api/usuario/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        UsuarioRequest invalido = new UsuarioRequest("abc", "123", null);

        mockMvc.perform(put("/api/usuario/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminar_deberiaRetornar204() throws Exception {
        doNothing().when(usuarioService).eliminar(5L);

        mockMvc.perform(delete("/api/usuario/5"))
                .andExpect(status().isNoContent());

        verify(usuarioService).eliminar(5L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminar_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(delete("/api/usuario/-1"))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).eliminar(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void activar_deberiaRetornar204() throws Exception {
        doNothing().when(usuarioService).activar(6L);

        mockMvc.perform(patch("/api/usuario/6/activar"))
                .andExpect(status().isNoContent());

        verify(usuarioService).activar(6L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void activar_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(patch("/api/usuario/0/activar"))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).activar(any());
    }

    private static UsuarioRequest usuarioRequestValido() {
        return new UsuarioRequest(
                "usuario.admin",
                "Password@123",
                1L
        );
    }

    private static UsuarioResponse usuarioResponse(Long id) {
        return new UsuarioResponse(
                id,
                "usuario" + id,
                1,
                "ACTIVO",
                LocalDateTime.of(2026, 1, 10, 8, 0),
                LocalDateTime.of(2026, 1, 10, 8, 30),
                1L,
                "ADMINISTRADOR"
        );
    }
}