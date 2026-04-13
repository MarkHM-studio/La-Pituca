package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TipoBilleteraVirtualRequest;
import com.restobar.lapituca.dto.response.TipoBilleteraVirtualResponse;
import com.restobar.lapituca.entity.TipoBilleteraVirtual;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TipoBilleteraVirtualRepository;
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
class TipoBilleteraVirtualServiceTest {

    @Mock
    private TipoBilleteraVirtualRepository tipoBilleteraVirtualRepository;

    @InjectMocks
    private TipoBilleteraVirtualService tipoBilleteraVirtualService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestValido() {
        TipoBilleteraVirtualRequest request = new TipoBilleteraVirtualRequest("YAPE");

        when(tipoBilleteraVirtualRepository.existsByNombre("YAPE")).thenReturn(false);
        when(tipoBilleteraVirtualRepository.save(any(TipoBilleteraVirtual.class))).thenAnswer(invocation -> {
            TipoBilleteraVirtual tipo = invocation.getArgument(0);
            tipo.setId(10L);
            tipo.setFechaHora_registro(LocalDateTime.of(2026, 4, 12, 18, 0));
            tipo.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 12, 18, 15));
            return tipo;
        });

        TipoBilleteraVirtualResponse response = tipoBilleteraVirtualService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals("YAPE", response.getNombre());
        assertNotNull(response.getFechaHora_Registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        TipoBilleteraVirtualRequest request = new TipoBilleteraVirtualRequest("YAPE");
        when(tipoBilleteraVirtualRepository.existsByNombre("YAPE")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> tipoBilleteraVirtualService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Tipo de Billetera Virtual con este nombre", ex.getMessage());
        verify(tipoBilleteraVirtualRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarTiposMapeados() {
        TipoBilleteraVirtual t1 = crearTipoBilletera(1L, "YAPE");
        TipoBilleteraVirtual t2 = crearTipoBilletera(2L, "PLIN");

        when(tipoBilleteraVirtualRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TipoBilleteraVirtualResponse> response = tipoBilleteraVirtualService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("YAPE", response.get(0).getNombre());
        assertEquals("PLIN", response.get(1).getNombre());
    }

    @Test
    void obtenerPorId_deberiaRetornarTipoBilletera_cuandoExiste() {
        TipoBilleteraVirtual tipo = crearTipoBilletera(5L, "LUKITA");
        when(tipoBilleteraVirtualRepository.findById(5L)).thenReturn(Optional.of(tipo));

        TipoBilleteraVirtualResponse response = tipoBilleteraVirtualService.obtenerPorId(5L);

        assertEquals(5L, response.getId());
        assertEquals("LUKITA", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(tipoBilleteraVirtualRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> tipoBilleteraVirtualService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Tipo de Billetera Virtual con id: 404 no encontrado", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        TipoBilleteraVirtual existente = crearTipoBilletera(7L, "BILLETERA_VIEJA");
        TipoBilleteraVirtualRequest request = new TipoBilleteraVirtualRequest("BILLETERA_NUEVA");

        when(tipoBilleteraVirtualRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(tipoBilleteraVirtualRepository.existsByNombreAndIdNot("BILLETERA_NUEVA", 7L)).thenReturn(false);
        when(tipoBilleteraVirtualRepository.save(existente)).thenReturn(existente);

        TipoBilleteraVirtualResponse response = tipoBilleteraVirtualService.actualizar(7L, request);

        assertEquals("BILLETERA_NUEVA", existente.getNombre());
        assertEquals(7L, response.getId());
        assertEquals("BILLETERA_NUEVA", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoTipoNoExiste() {
        TipoBilleteraVirtualRequest request = new TipoBilleteraVirtualRequest("BILLETERA_NUEVA");
        when(tipoBilleteraVirtualRepository.findById(700L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> tipoBilleteraVirtualService.actualizar(700L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(tipoBilleteraVirtualRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        TipoBilleteraVirtual existente = crearTipoBilletera(7L, "BILLETERA_VIEJA");
        TipoBilleteraVirtualRequest request = new TipoBilleteraVirtualRequest("BILLETERA_DUPLICADA");

        when(tipoBilleteraVirtualRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(tipoBilleteraVirtualRepository.existsByNombreAndIdNot("BILLETERA_DUPLICADA", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> tipoBilleteraVirtualService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Tipo de Billetera Virtual con ese nombre", ex.getMessage());
        verify(tipoBilleteraVirtualRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminarTipoBilletera_cuandoExiste() {
        TipoBilleteraVirtual tipo = crearTipoBilletera(9L, "BILLETERA_TEMP");
        when(tipoBilleteraVirtualRepository.findById(9L)).thenReturn(Optional.of(tipo));

        tipoBilleteraVirtualService.eliminar(9L);

        verify(tipoBilleteraVirtualRepository).delete(tipo);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(tipoBilleteraVirtualRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> tipoBilleteraVirtualService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Tipo de Billetera Virtual con id: 901 no encontrado", ex.getMessage());
        verify(tipoBilleteraVirtualRepository, never()).delete(any());
    }

    private TipoBilleteraVirtual crearTipoBilletera(Long id, String nombre) {
        TipoBilleteraVirtual tipo = new TipoBilleteraVirtual();
        tipo.setId(id);
        tipo.setNombre(nombre);
        tipo.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        tipo.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 30));
        return tipo;
    }
}