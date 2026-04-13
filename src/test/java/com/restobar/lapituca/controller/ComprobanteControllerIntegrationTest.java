package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.AsignarMesasRequest;
import com.restobar.lapituca.dto.request.ComprobanteRequest;
import com.restobar.lapituca.dto.request.RegistrarVentaRequest;
import com.restobar.lapituca.dto.response.ComprobanteDetalleResponse;
import com.restobar.lapituca.dto.response.ComprobanteListadoResponse;
import com.restobar.lapituca.dto.response.ComprobanteResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.ComprobanteService;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(ComprobanteController.class)
@Import(SecurityConfig.class)
class ComprobanteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComprobanteService comprobanteService;

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
    @WithMockUser(roles = "MOZO")
    void crear_deberiaRetornar201() throws Exception {
        when(comprobanteService.crearComprobante(any())).thenReturn(comprobanteResponse(1L));

        mockMvc.perform(post("/api/comprobante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComprobanteRequest(1L))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/comprobante/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void crear_deberiaRetornar400_cuandoRequestInvalido() throws Exception {
        mockMvc.perform(post("/api/comprobante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComprobanteRequest(null))))
                .andExpect(status().isBadRequest());

        verify(comprobanteService, never()).crearComprobante(any());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/comprobante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComprobanteRequest(1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/comprobante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComprobanteRequest(1L))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarTodos_deberiaRetornar200() throws Exception {
        when(comprobanteService.listarTodos()).thenReturn(List.of(comprobanteListadoResponse(2L)));

        mockMvc.perform(get("/api/comprobante"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void listarTodos_deberiaRetornar403_cuandoRolNoPermitido() throws Exception {
        mockMvc.perform(get("/api/comprobante"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void obtenerPorIdDetalle_deberiaRetornar200() throws Exception {
        when(comprobanteService.obtenerDetallePorId(3L)).thenReturn(comprobanteDetalleResponse(3L));

        mockMvc.perform(get("/api/comprobante/3/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void asignarMesas_deberiaRetornar200() throws Exception {
        when(comprobanteService.asignarGrupoYMesasSiEsComer(any())).thenReturn(comprobanteResponse(4L));

        mockMvc.perform(put("/api/comprobante/asignar-mesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AsignarMesasRequest(4L, Set.of(1L, 2L), "Grupo A"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void asignarMesas_deberiaRetornar400_cuandoRequestInvalido() throws Exception {
        mockMvc.perform(put("/api/comprobante/asignar-mesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AsignarMesasRequest(null, Set.of(), "Grupo demasiado largo para superar el limite de cincuenta caracteres en la validacion"))))
                .andExpect(status().isBadRequest());

        verify(comprobanteService, never()).asignarGrupoYMesasSiEsComer(any());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void asignarMesas_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(put("/api/comprobante/asignar-mesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AsignarMesasRequest(4L, Set.of(1L), "Grupo A"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void cancelarPedido_deberiaRetornar204() throws Exception {
        doNothing().when(comprobanteService).eliminar(5L);

        mockMvc.perform(delete("/api/comprobante/5"))
                .andExpect(status().isNoContent());

        verify(comprobanteService).eliminar(5L);
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void registrarVenta_deberiaRetornar200() throws Exception {
        when(comprobanteService.registrarVenta(any())).thenReturn("Venta registrada correctamente");

        mockMvc.perform(post("/api/comprobante/registrar-venta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrarVentaValido())))
                .andExpect(status().isOk())
                .andExpect(content().string("Venta registrada correctamente"));
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void registrarVenta_deberiaRetornar400_cuandoRequestInvalido() throws Exception {
        RegistrarVentaRequest invalido = new RegistrarVentaRequest(null, null, Set.of(), List.of(), null, "", "123", "123", null);

        mockMvc.perform(post("/api/comprobante/registrar-venta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(comprobanteService, never()).registrarVenta(any());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void registrarVenta_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/comprobante/registrar-venta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrarVentaValido())))
                .andExpect(status().isForbidden());
    }

    private static ComprobanteResponse comprobanteResponse(Long id) {
        return new ComprobanteResponse(
                id,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(18),
                LocalDateTime.of(2026, 1, 10, 12, 0),
                null,
                "ABIERTO",
                null
        );
    }

    private static ComprobanteListadoResponse comprobanteListadoResponse(Long id) {
        return new ComprobanteListadoResponse(
                id,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(18),
                LocalDateTime.of(2026, 1, 10, 12, 0),
                null,
                "ABIERTO",
                1L,
                null
        );
    }

    private static ComprobanteDetalleResponse comprobanteDetalleResponse(Long id) {
        return new ComprobanteDetalleResponse(
                id,
                BigDecimal.valueOf(118),
                BigDecimal.valueOf(18),
                BigDecimal.valueOf(100),
                LocalDateTime.of(2026, 1, 10, 12, 0),
                null,
                "ABIERTO",
                null,
                null,
                null,
                null,
                List.of(),
                List.of()
        );
    }

    private static RegistrarVentaRequest registrarVentaValido() {
        return new RegistrarVentaRequest(
                1L,
                1L,
                Set.of(1L),
                List.of(BigDecimal.valueOf(118)),
                null,
                "BOLETA",
                "12345678",
                null,
                1L
        );
    }
}