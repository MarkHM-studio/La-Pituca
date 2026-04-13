package com.restobar.lapituca.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.request.mercadopago.CrearPreferenciaPagoRequest;
import com.restobar.lapituca.dto.response.mercadopago.CrearPreferenciaPagoResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.MercadoPagoService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MercadoPagoController.class)
@Import(SecurityConfig.class)
class MercadoPagoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MercadoPagoService mercadoPagoService;

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
    @WithMockUser(roles = "CLIENTE")
    void crearPreferencia_deberiaRetornar200() throws Exception {
        when(mercadoPagoService.crearPreferenciaPago(any())).thenReturn(preferenciaResponse());

        mockMvc.perform(post("/api/mercadopago/preferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferenciaRequestValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaccionId").value(1))
                .andExpect(jsonPath("$.externalReference").value("TX-1"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void crearPreferencia_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
        CrearPreferenciaPagoRequest invalido = new CrearPreferenciaPagoRequest(null, "", BigDecimal.ZERO);

        mockMvc.perform(post("/api/mercadopago/preferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());

        verify(mercadoPagoService, never()).crearPreferenciaPago(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crearPreferencia_deberiaRetornar403_cuandoRolIncorrecto() throws Exception {
        mockMvc.perform(post("/api/mercadopago/preferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferenciaRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearPreferencia_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/mercadopago/preferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferenciaRequestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhook_deberiaRetornar200_sinAutenticacion() throws Exception {
        doNothing().when(mercadoPagoService).procesarWebhook(any(), eq("sig-123"), eq("req-456"));

        mockMvc.perform(post("/api/mercadopago/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-signature", "sig-123")
                        .header("x-request-id", "req-456")
                        .content(objectMapper.writeValueAsString(Map.of("type", "payment", "data", Map.of("id", "1001")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Webhook recibido"));

        verify(mercadoPagoService).procesarWebhook(any(), eq("sig-123"), eq("req-456"));
    }

    private static CrearPreferenciaPagoRequest preferenciaRequestValido() {
        return new CrearPreferenciaPagoRequest(
                10L,
                "Reserva mesa 10",
                BigDecimal.valueOf(100)
        );
    }

    private static CrearPreferenciaPagoResponse preferenciaResponse() {
        return new CrearPreferenciaPagoResponse(
                1L,
                10L,
                "TX-1",
                "pref-123",
                "https://mp.com/init",
                "https://mp.com/sandbox",
                "PENDIENTE"
        );
    }
}