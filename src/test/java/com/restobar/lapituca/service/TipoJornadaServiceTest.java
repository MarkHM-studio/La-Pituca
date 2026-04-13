package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TipoJornadaRequest;
import com.restobar.lapituca.dto.response.TipoJornadaResponse;
import com.restobar.lapituca.entity.TipoJornada;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TipoJornadaRepository;
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
class TipoJornadaServiceTest {

    @Mock
    private TipoJornadaRepository tipoJornadaRepository;

    @InjectMocks
    private TipoJornadaService tipoJornadaService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestValido() {
        TipoJornadaRequest request = new TipoJornadaRequest("Jornada Completa");

        when(tipoJornadaRepository.existsByNombre("Jornada Completa")).thenReturn(false);
        when(tipoJornadaRepository.save(any(TipoJornada.class))).thenAnswer(invocation -> {
            TipoJornada tipo = invocation.getArgument(0);
            tipo.setId(10L);
            tipo.setFechaHora_registro(LocalDateTime.of(2026, 4, 12, 12, 0));
            tipo.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 12, 12, 15));
            return tipo;
        });

        TipoJornadaResponse response = tipoJornadaService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals("Jornada Completa", response.getNombre());
        assertNotNull(response.getFechaHora_registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        TipoJornadaRequest request = new TipoJornadaRequest("Jornada Completa");
        when(tipoJornadaRepository.existsByNombre("Jornada Completa")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> tipoJornadaService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Tipo de Jornada con ese nombre", ex.getMessage());
        verify(tipoJornadaRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarTiposMapeados() {
        TipoJornada t1 = crearTipoJornada(1L, "Jornada Diurna");
        TipoJornada t2 = crearTipoJornada(2L, "Jornada Nocturna");

        when(tipoJornadaRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TipoJornadaResponse> response = tipoJornadaService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("Jornada Diurna", response.get(0).getNombre());
        assertEquals("Jornada Nocturna", response.get(1).getNombre());
    }

    @Test
    void obtenerPorId_deberiaRetornarTipoJornada_cuandoExiste() {
        TipoJornada tipo = crearTipoJornada(5L, "Jornada Mixta");
        when(tipoJornadaRepository.findById(5L)).thenReturn(Optional.of(tipo));

        TipoJornadaResponse response = tipoJornadaService.obtenerPorId(5L);

        assertEquals(5L, response.getId());
        assertEquals("Jornada Mixta", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(tipoJornadaRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> tipoJornadaService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Tipo de Jornada con id: 404 no encontrado", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        TipoJornada existente = crearTipoJornada(7L, "Jornada Antigua");
        TipoJornadaRequest request = new TipoJornadaRequest("Jornada Nueva");

        when(tipoJornadaRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(tipoJornadaRepository.existsByNombreAndIdNot("Jornada Nueva", 7L)).thenReturn(false);
        when(tipoJornadaRepository.save(existente)).thenReturn(existente);

        TipoJornadaResponse response = tipoJornadaService.actualizar(7L, request);

        assertEquals("Jornada Nueva", existente.getNombre());
        assertEquals(7L, response.getId());
        assertEquals("Jornada Nueva", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoTipoNoExiste() {
        TipoJornadaRequest request = new TipoJornadaRequest("Jornada Nueva");
        when(tipoJornadaRepository.findById(700L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> tipoJornadaService.actualizar(700L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(tipoJornadaRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        TipoJornada existente = crearTipoJornada(7L, "Jornada Antigua");
        TipoJornadaRequest request = new TipoJornadaRequest("Jornada Duplicada");

        when(tipoJornadaRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(tipoJornadaRepository.existsByNombreAndIdNot("Jornada Duplicada", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> tipoJornadaService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Tipo de Jornada con ese nombre", ex.getMessage());
        verify(tipoJornadaRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminarTipoJornada_cuandoExiste() {
        TipoJornada tipo = crearTipoJornada(9L, "Jornada Eventual");
        when(tipoJornadaRepository.findById(9L)).thenReturn(Optional.of(tipo));

        tipoJornadaService.eliminar(9L);

        verify(tipoJornadaRepository).delete(tipo);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(tipoJornadaRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> tipoJornadaService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Tipo de Jornada con id:901 no encontrado", ex.getMessage());
        verify(tipoJornadaRepository, never()).delete(any());
    }

    private TipoJornada crearTipoJornada(Long id, String nombre) {
        TipoJornada tipo = new TipoJornada();
        tipo.setId(id);
        tipo.setNombre(nombre);
        tipo.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        tipo.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 30));
        return tipo;
    }
}