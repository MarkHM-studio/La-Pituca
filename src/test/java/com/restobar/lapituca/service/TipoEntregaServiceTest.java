package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TipoEntregaRequest;
import com.restobar.lapituca.dto.response.TipoEntregaResponse;
import com.restobar.lapituca.entity.TipoEntrega;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TipoEntregaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoEntregaServiceTest {

    @Mock
    private TipoEntregaRepository tipoEntregaRepository;

    @InjectMocks
    private TipoEntregaService tipoEntregaService;

    // ===================== GUARDAR =====================

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestValido() {
        TipoEntregaRequest request = new TipoEntregaRequest("DELIVERY");

        when(tipoEntregaRepository.existsByNombre("DELIVERY")).thenReturn(false);
        when(tipoEntregaRepository.save(any(TipoEntrega.class))).thenAnswer(invocation -> {
            TipoEntrega tipo = invocation.getArgument(0);
            tipo.setId(10L);
            tipo.setFechaHora_registro(LocalDateTime.of(2026, 4, 12, 18, 0));
            tipo.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 12, 18, 15));
            return tipo;
        });

        TipoEntregaResponse response = tipoEntregaService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals("DELIVERY", response.getNombre());
        assertNotNull(response.getFechaHora_Registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        TipoEntregaRequest request = new TipoEntregaRequest("DELIVERY");

        when(tipoEntregaRepository.existsByNombre("DELIVERY")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> tipoEntregaService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Tipo de Entrega con este nombre", ex.getMessage());
        verify(tipoEntregaRepository, never()).save(any());
    }

    // ===================== LISTAR =====================

    @Test
    void listarTodos_deberiaRetornarTiposMapeados() {
        TipoEntrega t1 = crearTipoEntrega(1L, "DELIVERY");
        TipoEntrega t2 = crearTipoEntrega(2L, "RECOJO");

        when(tipoEntregaRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TipoEntregaResponse> response = tipoEntregaService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("DELIVERY", response.get(0).getNombre());
        assertEquals("RECOJO", response.get(1).getNombre());
    }

    // ===================== OBTENER POR ID =====================

    @Test
    void obtenerPorId_deberiaRetornarTipoEntrega_cuandoExiste() {
        TipoEntrega tipo = crearTipoEntrega(5L, "DELIVERY");

        when(tipoEntregaRepository.findById(5L)).thenReturn(Optional.of(tipo));

        TipoEntregaResponse response = tipoEntregaService.obtenerPorId(5L);

        assertEquals(5L, response.getId());
        assertEquals("DELIVERY", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(tipoEntregaRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> tipoEntregaService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Tipo de Entrega con id: 404 no encontrada", ex.getMessage());
    }

    // ===================== ACTUALIZAR =====================

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        TipoEntrega existente = crearTipoEntrega(7L, "ANTIGUO");
        TipoEntregaRequest request = new TipoEntregaRequest("NUEVO");

        when(tipoEntregaRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(tipoEntregaRepository.existsByNombreAndIdNot("NUEVO", 7L)).thenReturn(false);
        when(tipoEntregaRepository.save(existente)).thenReturn(existente);

        TipoEntregaResponse response = tipoEntregaService.actualizar(7L, request);

        assertEquals("NUEVO", existente.getNombre());
        assertEquals(7L, response.getId());
        assertEquals("NUEVO", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoTipoNoExiste() {
        TipoEntregaRequest request = new TipoEntregaRequest("NUEVO");

        when(tipoEntregaRepository.findById(700L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> tipoEntregaService.actualizar(700L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(tipoEntregaRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        TipoEntrega existente = crearTipoEntrega(7L, "ANTIGUO");
        TipoEntregaRequest request = new TipoEntregaRequest("DUPLICADO");

        when(tipoEntregaRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(tipoEntregaRepository.existsByNombreAndIdNot("DUPLICADO", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> tipoEntregaService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Tipo de Entrega con este nombre", ex.getMessage());
        verify(tipoEntregaRepository, never()).save(any());
    }

    // ===================== ELIMINAR =====================

    @Test
    void eliminar_deberiaEliminarTipoEntrega_cuandoExiste() {
        TipoEntrega tipo = crearTipoEntrega(9L, "TEMP");

        when(tipoEntregaRepository.findById(9L)).thenReturn(Optional.of(tipo));

        tipoEntregaService.eliminar(9L);

        verify(tipoEntregaRepository).delete(tipo);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(tipoEntregaRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> tipoEntregaService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Tipo de Entrega con id: 901 no encontrada", ex.getMessage());
        verify(tipoEntregaRepository, never()).delete(any());
    }

    // ===================== HELPER =====================

    private TipoEntrega crearTipoEntrega(Long id, String nombre) {
        TipoEntrega tipo = new TipoEntrega();
        tipo.setId(id);
        tipo.setNombre(nombre);
        tipo.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        tipo.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 30));
        return tipo;
    }
}