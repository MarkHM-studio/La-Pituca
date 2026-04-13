package com.restobar.lapituca.controller;

import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.PedidoRequest;
import com.restobar.lapituca.dto.response.ComprobanteResponse;
import com.restobar.lapituca.dto.response.PedidoDetalleResponse;
import com.restobar.lapituca.dto.response.PedidoResponse;
import com.restobar.lapituca.dto.response.ProductoResponse;
import com.restobar.lapituca.dto.response.TipoEntregaResponse;
import com.restobar.lapituca.dto.response.UsuarioResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.PedidoService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(PedidoController.class)
@Import(SecurityConfig.class)
class PedidoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

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
        when(pedidoService.guardar(any())).thenReturn(pedidoDetalleResponse(1L));

        mockMvc.perform(post("/api/pedido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyValido()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pedido/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void crear_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        mockMvc.perform(post("/api/pedido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cantidad": 0,
                                  "comprobanteId": 0,
                                  "productoId": -1,
                                  "tipoEntregaId": null,
                                  "usuarioId": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(pedidoService, never()).guardar(any());
    }

    @Test
    @WithMockUser(roles = "COCINERO")
    void crear_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/pedido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyValido()))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/pedido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyValido()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "COCINERO")
    void listarTodos_deberiaRetornar200() throws Exception {
        when(pedidoService.listarTodos()).thenReturn(List.of(pedidoResponse(11L)));

        mockMvc.perform(get("/api/pedido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11));
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void listarTodos_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(get("/api/pedido"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "BARTENDER")
    void obtenerPorId_deberiaRetornar200() throws Exception {
        when(pedidoService.obtenerPorId(5L)).thenReturn(pedidoResponse(5L));

        mockMvc.perform(get("/api/pedido/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(roles = "BARTENDER")
    void obtenerPorId_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(get("/api/pedido/0"))
                .andExpect(status().isBadRequest());

        verify(pedidoService, never()).obtenerPorId(any());
    }

    @Test
    @WithMockUser(roles = "COCINERO")
    void obtenerDetallePorId_deberiaRetornar200() throws Exception {
        when(pedidoService.obtenerDetallePorId(3L)).thenReturn(pedidoDetalleResponse(3L));

        mockMvc.perform(get("/api/pedido/3/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void obtenerPorComprobanteId_deberiaRetornar200() throws Exception {
        when(pedidoService.obtenerPorComprobanteId(8L)).thenReturn(List.of(pedidoResponse(21L)));

        mockMvc.perform(get("/api/pedido/comprobante/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(21));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void obtenerDetallePorComprobanteId_deberiaRetornar200() throws Exception {
        when(pedidoService.obtenerDetallePorComprobanteId(9L)).thenReturn(List.of(pedidoDetalleResponse(31L)));

        mockMvc.perform(get("/api/pedido/comprobante/9/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(31));
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void actualizar_deberiaRetornar200() throws Exception {
        when(pedidoService.actualizar(eq(4L), any(PedidoRequest.class))).thenReturn(pedidoDetalleResponse(4L));

        mockMvc.perform(put("/api/pedido/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyValido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void actualizar_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(put("/api/pedido/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyValido()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void eliminar_deberiaRetornar204() throws Exception {
        doNothing().when(pedidoService).eliminar(7L);

        mockMvc.perform(delete("/api/pedido/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void eliminar_deberiaRetornar400_cuandoIdInvalido() throws Exception {
        mockMvc.perform(delete("/api/pedido/-5"))
                .andExpect(status().isBadRequest());

        verify(pedidoService, never()).eliminar(any());
    }

    @Test
    @WithMockUser(roles = "COCINERO")
    void marcarComoListo_deberiaRetornar200() throws Exception {
        doNothing().when(pedidoService).marcarComoListo(2L);

        mockMvc.perform(put("/api/pedido/2/listo"))
                .andExpect(status().isOk())
                .andExpect(content().string("Pedido marcado como 'LISTO' correctamente"));
    }

    @Test
    @WithMockUser(roles = "BARTENDER")
    void marcarComoPreparando_deberiaRetornar200() throws Exception {
        doNothing().when(pedidoService).marcarComoPreparando(6L);

        mockMvc.perform(put("/api/pedido/6/preparando"))
                .andExpect(status().isOk())
                .andExpect(content().string("Pedido marcado como 'PREPARANDO' correctamente"));
    }

    @Test
    @WithMockUser(roles = "MOZO")
    void marcarComoEntregado_deberiaRetornar200() throws Exception {
        doNothing().when(pedidoService).marcarComoEntregado(10L);

        mockMvc.perform(put("/api/pedido/10/entregado"))
                .andExpect(status().isOk())
                .andExpect(content().string("Pedido marcado como 'ENTREGADO' correctamente"));
    }

    private static String requestBodyValido() {
        return """
                {
                  "cantidad": 2,
                  "comprobanteId": 1,
                  "productoId": 1,
                  "tipoEntregaId": 1,
                  "usuarioId": 1
                }
                """;
    }

    private static PedidoResponse pedidoResponse(Long id) {
        return new PedidoResponse(
                id,
                2,
                BigDecimal.valueOf(10.50),
                BigDecimal.valueOf(21.00),
                "PENDIENTE",
                LocalDateTime.of(2026, 1, 10, 12, 30),
                1L,
                1L,
                1L,
                1L
        );
    }

    private static PedidoDetalleResponse pedidoDetalleResponse(Long id) {
        return new PedidoDetalleResponse(
                id,
                2,
                BigDecimal.valueOf(21.00),
                "PENDIENTE",
                LocalDateTime.of(2026, 1, 10, 12, 30),
                new ProductoResponse(1L, "Lomo", BigDecimal.valueOf(10.50), 20, null, null),
                new ComprobanteResponse(1L, BigDecimal.valueOf(21.00), BigDecimal.valueOf(3.78), LocalDateTime.of(2026, 1, 10, 12, 0), null, "ABIERTO", null),
                new TipoEntregaResponse(1L, "SALON", LocalDateTime.of(2026, 1, 10, 11, 0), LocalDateTime.of(2026, 1, 10, 11, 0)),
                new UsuarioResponse(1L, "mozo1", 2, "ACTIVO", LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 10, 0), 1L, "MOZO")
        );
    }
}
