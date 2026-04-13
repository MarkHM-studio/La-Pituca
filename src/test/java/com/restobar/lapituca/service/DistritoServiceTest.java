package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.DistritoRequest;
import com.restobar.lapituca.dto.response.DistritoResponse;
import com.restobar.lapituca.entity.Distrito;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.DistritoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistritoServiceTest {

    @Mock
    private DistritoRepository distritoRepository;

    @InjectMocks
    private DistritoService distritoService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoValido() {
        DistritoRequest request = new DistritoRequest("Miraflores");

        when(distritoRepository.existsByNombreIgnoreCase("Miraflores")).thenReturn(false);
        when(distritoRepository.save(any(Distrito.class))).thenAnswer(invocation -> {
            Distrito d = invocation.getArgument(0);
            d.setId(1L);
            d.setFechaHora_registro(LocalDateTime.now());
            return d;
        });

        DistritoResponse response = distritoService.guardar(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Miraflores", response.getNombre());
    }

    @Test
    void guardar_deberiaLanzarError_cuandoNombreDuplicado() {
        DistritoRequest request = new DistritoRequest("Miraflores");

        when(distritoRepository.existsByNombreIgnoreCase("Miraflores")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> distritoService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un distrito con ese nombre", ex.getMessage());
        verify(distritoRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarLista() {
        Distrito d1 = crearDistrito(1L, "Miraflores");
        Distrito d2 = crearDistrito(2L, "Surco");

        when(distritoRepository.findAll()).thenReturn(List.of(d1, d2));

        List<DistritoResponse> response = distritoService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("Miraflores", response.get(0).getNombre());
        assertEquals("Surco", response.get(1).getNombre());
    }

    @Test
    void obtenerPorId_deberiaRetornar_cuandoExiste() {
        Distrito distrito = crearDistrito(1L, "Barranco");

        when(distritoRepository.findById(1L)).thenReturn(Optional.of(distrito));

        DistritoResponse response = distritoService.obtenerPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("Barranco", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaLanzarError_cuandoNoExiste() {
        when(distritoRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> distritoService.obtenerPorId(1L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Distrito con id: 1 no encontrado", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizar_cuandoValido() {
        Distrito existente = crearDistrito(1L, "Miraflores");
        DistritoRequest request = new DistritoRequest("San Isidro");

        when(distritoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(distritoRepository.existsByNombreIgnoreCaseAndIdNot("San Isidro", 1L)).thenReturn(false);
        when(distritoRepository.save(existente)).thenReturn(existente);

        DistritoResponse response = distritoService.actualizar(1L, request);

        assertEquals("San Isidro", existente.getNombre());
        assertEquals(1L, response.getId());
        assertEquals("San Isidro", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarError_cuandoNoExiste() {
        DistritoRequest request = new DistritoRequest("San Isidro");

        when(distritoRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> distritoService.actualizar(1L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(distritoRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarError_cuandoNombreDuplicado() {
        Distrito existente = crearDistrito(1L, "Miraflores");
        DistritoRequest request = new DistritoRequest("Surco");

        when(distritoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(distritoRepository.existsByNombreIgnoreCaseAndIdNot("Surco", 1L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> distritoService.actualizar(1L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un distrito con ese nombre", ex.getMessage());
        verify(distritoRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminar_cuandoExiste() {
        Distrito distrito = crearDistrito(1L, "Miraflores");

        when(distritoRepository.findById(1L)).thenReturn(Optional.of(distrito));

        distritoService.eliminar(1L);

        verify(distritoRepository).delete(distrito);
    }

    @Test
    void eliminar_deberiaLanzarError_cuandoNoExiste() {
        when(distritoRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> distritoService.eliminar(1L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Distrito con id: 1 no encontrado", ex.getMessage());
        verify(distritoRepository, never()).delete(any());
    }

    // 🔧 helper
    private Distrito crearDistrito(Long id, String nombre) {
        Distrito d = new Distrito();
        d.setId(id);
        d.setNombre(nombre);
        d.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        d.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 11, 0));
        return d;
    }
}