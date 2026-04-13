package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.ProductoRequest;
import com.restobar.lapituca.dto.response.ProductoResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.ProductoService;
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

@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class ProductoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

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
        when(productoService.guardar(any())).thenReturn(productoResponse(1L));

        mockMvc.perform(post("/api/producto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/producto/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void crear_deberiaRetornar201_paraAlmacenero() throws Exception {
        when(productoService.guardar(any())).thenReturn(productoResponse(2L));

        mockMvc.perform(post("/api/producto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        ProductoRequest invalido = new ProductoRequest("abc", BigDecimal.valueOf(-1), -1, null, null);

        mockMvc.perform(post("/api/producto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).guardar(any());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/producto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/producto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoRequestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void listarTodos_deberiaRetornar200_paraMozo() throws Exception {
        when(productoService.listarTodos()).thenReturn(List.of(productoResponse(3L)));

        mockMvc.perform(get("/api/producto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void listarTodos_deberiaRetornar403_cuandoRolNoPermitido() throws Exception {
        mockMvc.perform(get("/api/producto"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void obtenerPorId_deberiaRetornar200() throws Exception {
        when(productoService.obtenerPorId(4L)).thenReturn(productoResponse(4L));

        mockMvc.perform(get("/api/producto/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void obtenerPorId_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(get("/api/producto/0"))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void obtenerPorId_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(get("/api/producto/4"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar200() throws Exception {
        when(productoService.actualizar(eq(5L), any())).thenReturn(productoResponse(5L));

        mockMvc.perform(put("/api/producto/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoRequestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizar_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        ProductoRequest invalido = new ProductoRequest("abc", BigDecimal.ZERO, -1, null, null);

        mockMvc.perform(put("/api/producto/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).actualizar(any(), any());
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void eliminar_deberiaRetornar204() throws Exception {
        doNothing().when(productoService).eliminar(6L);

        mockMvc.perform(delete("/api/producto/6"))
                .andExpect(status().isNoContent());

        verify(productoService).eliminar(6L);
    }

    @Test
    @WithMockUser(roles = "ALMACENERO")
    void eliminar_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(delete("/api/producto/-1"))
                .andExpect(status().isBadRequest());

        verify(productoService, never()).eliminar(any());
    }

    private static ProductoRequest productoRequestValido() {
        return new ProductoRequest(
                "Inca Kola 500ml",
                BigDecimal.valueOf(7.50),
                30,
                1L,
                1L
        );
    }

    private static ProductoResponse productoResponse(Long id) {
        return new ProductoResponse(
                id,
                "Producto " + id,
                BigDecimal.valueOf(10.50),
                20,
                null,
                null
        );
    }
}