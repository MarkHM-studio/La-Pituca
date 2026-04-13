package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TipoPagoRequest;
import com.restobar.lapituca.dto.response.TipoPagoResponse;
import com.restobar.lapituca.entity.TipoPago;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TipoPagoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoPagoServiceTest {

    @Mock
    private TipoPagoRepository tipoPagoRepository;

    @InjectMocks
    private TipoPagoService tipoPagoService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestValido() {
        TipoPagoRequest request = new TipoPagoRequest("EFECTIVO");

        when(tipoPagoRepository.existsByNombre("EFECTIVO")).thenReturn(false);
        when(tipoPagoRepository.save(any(TipoPago.class))).thenAnswer(invocation -> {
            TipoPago tipoPago = invocation.getArgument(0);
            tipoPago.setId(10L);
            tipoPago.setFechaHora_registro(LocalDateTime.of(2026, 4, 12, 17, 0));
            tipoPago.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 12, 17, 15));
            return tipoPago;
        });

        TipoPagoResponse response = tipoPagoService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals("EFECTIVO", response.getNombre());
        assertNotNull(response.getFechaHora_Registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        TipoPagoRequest request = new TipoPagoRequest("EFECTIVO");
        when(tipoPagoRepository.existsByNombre("EFECTIVO")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> tipoPagoService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Tipo de Pago con este nombre", ex.getMessage());
        verify(tipoPagoRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarTiposMapeados() {
        TipoPago t1 = crearTipoPago(1L, "EFECTIVO");
        TipoPago t2 = crearTipoPago(2L, "BILLETERA");

        when(tipoPagoRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TipoPagoResponse> response = tipoPagoService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("EFECTIVO", response.get(0).getNombre());
        assertEquals("BILLETERA", response.get(1).getNombre());
    }

    @Test
    void obtenerPorId_deberiaRetornarTipoPago_cuandoExiste() {
        TipoPago tipoPago = crearTipoPago(5L, "TARJETA");
        when(tipoPagoRepository.findById(5L)).thenReturn(Optional.of(tipoPago));

        TipoPagoResponse response = tipoPagoService.obtenerPorId(5L);

        assertEquals(5L, response.getId());
        assertEquals("TARJETA", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(tipoPagoRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> tipoPagoService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Tipo de Pago con id: 404 no encontrada", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        TipoPago existente = crearTipoPago(7L, "PAGO_VIEJO");
        TipoPagoRequest request = new TipoPagoRequest("PAGO_NUEVO");

        when(tipoPagoRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(tipoPagoRepository.existsByNombreAndIdNot("PAGO_NUEVO", 7L)).thenReturn(false);
        when(tipoPagoRepository.save(existente)).thenReturn(existente);

        TipoPagoResponse response = tipoPagoService.actualizar(7L, request);

        assertEquals("PAGO_NUEVO", existente.getNombre());
        assertEquals(7L, response.getId());
        assertEquals("PAGO_NUEVO", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoTipoPagoNoExiste() {
        TipoPagoRequest request = new TipoPagoRequest("PAGO_NUEVO");
        when(tipoPagoRepository.findById(700L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> tipoPagoService.actualizar(700L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(tipoPagoRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        TipoPago existente = crearTipoPago(7L, "PAGO_VIEJO");
        TipoPagoRequest request = new TipoPagoRequest("PAGO_DUPLICADO");

        when(tipoPagoRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(tipoPagoRepository.existsByNombreAndIdNot("PAGO_DUPLICADO", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> tipoPagoService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Tipo de Pago con este nombre", ex.getMessage());
        verify(tipoPagoRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminarTipoPago_cuandoExiste() {
        TipoPago tipoPago = crearTipoPago(9L, "PAGO_TEMP");
        when(tipoPagoRepository.findById(9L)).thenReturn(Optional.of(tipoPago));

        tipoPagoService.eliminar(9L);

        verify(tipoPagoRepository).delete(tipoPago);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(tipoPagoRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> tipoPagoService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Tipo de Pago con id: 901 no encontrada", ex.getMessage());
        verify(tipoPagoRepository, never()).delete(any());
    }

    private TipoPago crearTipoPago(Long id, String nombre) {
        TipoPago tipoPago = new TipoPago();
        tipoPago.setId(id);
        tipoPago.setNombre(nombre);
        tipoPago.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        tipoPago.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 30));
        return tipoPago;
    }
}