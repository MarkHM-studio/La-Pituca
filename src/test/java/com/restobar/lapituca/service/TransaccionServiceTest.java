package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.response.TransaccionResponse;
import com.restobar.lapituca.entity.Reserva;
import com.restobar.lapituca.entity.Transaccion;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TransaccionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private TransaccionService transaccionService;

    private static final BigDecimal MONTO = BigDecimal.valueOf(100.00);

    @Test
    void listarTodos_deberiaRetornarListaDeTransacciones() {
        Transaccion t1 = crearTransaccion(1L);
        Transaccion t2 = crearTransaccion(2L);

        when(transaccionRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TransaccionResponse> response = transaccionService.listarTodos();

        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(2L, response.get(1).getId());
    }

    @Test
    void obtenerPorId_deberiaRetornarTransaccion_cuandoExiste() {
        Transaccion transaccion = crearTransaccion(10L);

        when(transaccionRepository.findById(10L)).thenReturn(Optional.of(transaccion));

        TransaccionResponse response = transaccionService.obtenerPorId(10L);

        assertEquals(10L, response.getId());
        assertEquals("MP123", response.getMercadoPagoPaymentId());
        assertEquals(MONTO, response.getMonto());
        assertEquals(1L, response.getUsuarioId());
        assertEquals(1L, response.getReservaId());
    }

    @Test
    void obtenerPorId_deberiaLanzarError_cuandoNoExiste() {
        when(transaccionRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> transaccionService.obtenerPorId(99L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Transacción con id: 99 no encontrada", ex.getMessage());
    }

    // 🔧 helper
    private Transaccion crearTransaccion(Long id) {
        Transaccion t = new Transaccion();
        t.setId(id);
        t.setMercadoPagoPaymentId("MP123");
        t.setMercadoPagoPreferenceId("PREF123");
        t.setExternalReference("EXT123");
        t.setEstado("APROBADO");
        t.setEstadoMercadoPago("approved");
        t.setDetalleEstadoMercadoPago("accredited");
        t.setMonto(MONTO);
        t.setFechaPago(LocalDateTime.of(2026, 4, 1, 10, 0));
        t.setFechaActualizacion(LocalDateTime.of(2026, 4, 1, 11, 0));

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Reserva reserva = new Reserva();
        reserva.setId(1L);

        t.setUsuario(usuario);
        t.setReserva(reserva);

        return t;
    }
}