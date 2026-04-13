package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.ProveedorRequest;
import com.restobar.lapituca.dto.response.ProveedorResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.ProveedorService;
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

@WebMvcTest(ProveedorController.class)
@Import(SecurityConfig.class)
class ProveedorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProveedorService proveedorService;

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
    void crear_deberiaRetornar201_paraAdministrador() throws Exception {
        when(proveedorService.guardar(any())).thenReturn(proveedorResponse(1L));

        mockMvc.perform(post("/api/proveedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedorRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/proveedor/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void crear_deberiaRetornar201_paraAlmacenero() throws Exception {
        when(proveedorService.guardar(any())).thenReturn(proveedorResponse(2L));

        mockMvc.perform(post("/api/proveedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedorRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        ProveedorRequest invalido = new ProveedorRequest("A", "RS", "123", "Dir", "123", "correo", "ACT");

        mockMvc.perform(post("/api/proveedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(proveedorService, never()).guardar(any());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/proveedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedorRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/proveedor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedorRequestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200() throws Exception {
        when(proveedorService.listarTodos()).thenReturn(List.of(proveedorResponse(3L)));

        mockMvc.perform(get("/api/proveedor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void listarTodos_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(get("/api/proveedor"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void obtenerPorId_deberiaRetornar200() throws Exception {
        when(proveedorService.obtenerPorId(4L)).thenReturn(proveedorResponse(4L));

        mockMvc.perform(get("/api/proveedor/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void obtenerPorId_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(get("/api/proveedor/0"))
                .andExpect(status().isBadRequest());

        verify(proveedorService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar200() throws Exception {
        when(proveedorService.actualizar(eq(5L), any())).thenReturn(proveedorResponse(5L));

        mockMvc.perform(put("/api/proveedor/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedorRequestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        ProveedorRequest invalido = new ProveedorRequest("A", "RS", "123", "Dir", "123", "correo", "ACT");

        mockMvc.perform(put("/api/proveedor/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(proveedorService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void eliminar_deberiaRetornar204() throws Exception {
        doNothing().when(proveedorService).eliminar(6L);

        mockMvc.perform(delete("/api/proveedor/6"))
                .andExpect(status().isNoContent());

        verify(proveedorService).eliminar(6L);
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void eliminar_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(delete("/api/proveedor/-1"))
                .andExpect(status().isBadRequest());

        verify(proveedorService, never()).eliminar(any());
    }

    private static ProveedorRequest proveedorRequestValido() {
        return new ProveedorRequest(
                "Juan Perez",
                "Distribuidora Lima SAC",
                "12345678901",
                "Av. Comercio 123",
                "999888777",
                "proveedor@mail.com",
                "ACTIVO"
        );
    }

    private static ProveedorResponse proveedorResponse(Long id) {
        return new ProveedorResponse(
                id,
                "Juan Perez",
                "Distribuidora Lima SAC",
                "12345678901",
                "Av. Comercio 123",
                "999888777",
                "proveedor@mail.com",
                "ACTIVO",
                LocalDateTime.of(2026, 1, 10, 8, 0),
                LocalDateTime.of(2026, 1, 10, 8, 30)
        );
    }
}