package com.restobar.lapituca.service;

import com.mercadopago.resources.payment.Payment;
import com.restobar.lapituca.dto.request.mercadopago.CrearPreferenciaPagoRequest;
import com.restobar.lapituca.entity.Reserva;
import com.restobar.lapituca.entity.Transaccion;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ReservaRepository;
import com.restobar.lapituca.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private TransaccionRepository transaccionRepository;

    @InjectMocks private MercadoPagoService mercadoPagoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mercadoPagoService, "notificationUrl", "https://api.test/webhook");
        ReflectionTestUtils.setField(mercadoPagoService, "successUrl", "https://front.test/success");
        ReflectionTestUtils.setField(mercadoPagoService, "pendingUrl", "https://front.test/pending");
        ReflectionTestUtils.setField(mercadoPagoService, "failureUrl", "https://front.test/failure");
        ReflectionTestUtils.setField(mercadoPagoService, "webhookSecret", "");
    }

    @Test
    void crearPreferenciaPago_deberiaFallarSiReservaNoExiste() {
        CrearPreferenciaPagoRequest request = new CrearPreferenciaPagoRequest(1L, "Pago reserva", new BigDecimal("25.00"));
        when(reservaRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> mercadoPagoService.crearPreferenciaPago(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    void crearPreferenciaPago_deberiaFallarSiReservaYaPagada() {
        CrearPreferenciaPagoRequest request = new CrearPreferenciaPagoRequest(2L, "Pago reserva", new BigDecimal("25.00"));
        Reserva reserva = reserva(2L, "PAGADO", LocalDateTime.now().plusMinutes(10));
        when(reservaRepository.findById(2L)).thenReturn(Optional.of(reserva));

        ApiException ex = assertThrows(ApiException.class, () -> mercadoPagoService.crearPreferenciaPago(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("ya está pagada"));
    }

    @Test
    void crearPreferenciaPago_deberiaFallarSiReservaCanceladaOExpirada() {
        CrearPreferenciaPagoRequest request = new CrearPreferenciaPagoRequest(3L, "Pago reserva", new BigDecimal("25.00"));
        Reserva reserva = reserva(3L, "CANCELADO", LocalDateTime.now().plusMinutes(10));
        when(reservaRepository.findById(3L)).thenReturn(Optional.of(reserva));

        ApiException ex = assertThrows(ApiException.class, () -> mercadoPagoService.crearPreferenciaPago(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("cancelada o expirada"));
    }

    @Test
    void crearPreferenciaPago_deberiaExpirarReservaSiFechaExpirada() {
        CrearPreferenciaPagoRequest request = new CrearPreferenciaPagoRequest(4L, "Pago reserva", new BigDecimal("25.00"));
        Reserva reserva = reserva(4L, "ESPERANDO PAGO", LocalDateTime.now().minusMinutes(1));
        when(reservaRepository.findById(4L)).thenReturn(Optional.of(reserva));

        ApiException ex = assertThrows(ApiException.class, () -> mercadoPagoService.crearPreferenciaPago(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("EXPIRADO", reserva.getEstado());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void procesarWebhook_deberiaIgnorarPayloadSinPaymentId() {
        mercadoPagoService.procesarWebhook(Map.of("type", "other", "data", Map.of("id", "999")), null, null);

        verifyNoInteractions(transaccionRepository);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void procesarWebhook_deberiaIgnorarPayloadVacio() {
        mercadoPagoService.procesarWebhook(Map.of("hello", "world"), null, null);

        verifyNoInteractions(transaccionRepository);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void mapearEstadoInterno_deberiaMapearEstadosConocidosYDesconocido() {
        String aprobado = ReflectionTestUtils.invokeMethod(mercadoPagoService, "mapearEstadoInterno", "approved");
        String pendiente = ReflectionTestUtils.invokeMethod(mercadoPagoService, "mapearEstadoInterno", "pending");
        String rechazado = ReflectionTestUtils.invokeMethod(mercadoPagoService, "mapearEstadoInterno", "rejected");
        String desconocido = ReflectionTestUtils.invokeMethod(mercadoPagoService, "mapearEstadoInterno", "unknown_status");
        String nulo = ReflectionTestUtils.invokeMethod(mercadoPagoService, "mapearEstadoInterno", new Object[]{null});

        assertEquals("PAGO_APROBADO", aprobado);
        assertEquals("PAGO_PENDIENTE", pendiente);
        assertEquals("PAGO_RECHAZADO", rechazado);
        assertEquals("PAGO_DESCONOCIDO", desconocido);
        assertEquals("PAGO_DESCONOCIDO", nulo);
    }

    @Test
    void obtenerReservaPorExternalReference_deberiaFallarSiNoEsNumero() {
        ApiException ex = assertThrows(ApiException.class,
                () -> ReflectionTestUtils.invokeMethod(mercadoPagoService, "obtenerReservaPorExternalReference", "ABC"));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
    }

    @Test
    void actualizarTransaccionYReserva_deberiaMarcarPagadoCuandoAprobado() {
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(12345L);
        when(payment.getExternalReference()).thenReturn("10");
        when(payment.getStatus()).thenReturn("approved");
        when(payment.getStatusDetail()).thenReturn("accredited");
        when(payment.getTransactionAmount()).thenReturn(new BigDecimal("18.50"));

        Usuario usuario = new Usuario();
        usuario.setId(99L);

        Reserva reserva = reserva(10L, "ESPERANDO PAGO", LocalDateTime.now().plusMinutes(10));
        reserva.setUsuario(usuario);

        Transaccion tx = new Transaccion();
        tx.setExternalReference("10");
        tx.setReserva(reserva);
        tx.setMonto(new BigDecimal("18.50"));

        when(transaccionRepository.findByMercadoPagoPaymentId("12345")).thenReturn(Optional.empty());
        when(transaccionRepository.findTopByExternalReferenceOrderByFechaActualizacionDesc("10")).thenReturn(Optional.of(tx));

        ReflectionTestUtils.invokeMethod(mercadoPagoService, "actualizarTransaccionYReserva", payment);

        assertEquals("PAGO_APROBADO", tx.getEstado());
        assertEquals("approved", tx.getEstadoMercadoPago());
        assertEquals("12345", tx.getMercadoPagoPaymentId());
        assertEquals("PAGADO", reserva.getEstado());
        assertNull(reserva.getFechaHora_expiracionPago());
        assertNotNull(tx.getUsuario());

        verify(transaccionRepository).save(tx);
        verify(reservaRepository).save(reserva);
    }

    @Test
    void esWebhookValido_deberiaRetornarTrueSiSecretVacio() {
        ReflectionTestUtils.setField(mercadoPagoService, "webhookSecret", "");

        Boolean valido = ReflectionTestUtils.invokeMethod(
                mercadoPagoService,
                "esWebhookValido",
                Map.of("data", Map.of("id", "123")),
                "ts=1,v1=hash",
                "req-1"
        );

        assertTrue(valido);
    }

    @Test
    void esWebhookValido_deberiaRetornarFalseSiFaltaFirmaConSecretConfigurado() {
        ReflectionTestUtils.setField(mercadoPagoService, "webhookSecret", "secreto");

        Boolean valido = ReflectionTestUtils.invokeMethod(
                mercadoPagoService,
                "esWebhookValido",
                Map.of("data", Map.of("id", "123")),
                null,
                "req-1"
        );

        assertFalse(valido);
    }

    private Reserva reserva(Long id, String estado, LocalDateTime expiracion) {
        Reserva reserva = new Reserva();
        reserva.setId(id);
        reserva.setEstado(estado);
        reserva.setFechaHora_expiracionPago(expiracion);
        return reserva;
    }
}