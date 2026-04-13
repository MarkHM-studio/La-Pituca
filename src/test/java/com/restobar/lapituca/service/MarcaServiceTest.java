package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.MarcaRequest;
import com.restobar.lapituca.dto.response.MarcaResponse;
import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.MarcaRepository;
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
class MarcaServiceTest {

    @Mock
    private MarcaRepository marcaRepository;

    @InjectMocks
    private MarcaService marcaService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestValido() {
        MarcaRequest request = new MarcaRequest("Coca Cola");

        when(marcaRepository.existsByNombre("Coca Cola")).thenReturn(false);
        when(marcaRepository.save(any(Marca.class))).thenAnswer(invocation -> {
            Marca marca = invocation.getArgument(0);
            marca.setId(10L);
            marca.setFechaHora_registro(LocalDateTime.of(2026, 4, 12, 14, 0));
            marca.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 12, 14, 15));
            return marca;
        });

        MarcaResponse response = marcaService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals("Coca Cola", response.getNombre());
        assertNotNull(response.getFechaHora_Registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        MarcaRequest request = new MarcaRequest("Coca Cola");
        when(marcaRepository.existsByNombre("Coca Cola")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> marcaService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe una Marca con ese nombre", ex.getMessage());
        verify(marcaRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarMarcasMapeadas() {
        Marca m1 = crearMarca(1L, "Coca Cola");
        Marca m2 = crearMarca(2L, "Pepsi");

        when(marcaRepository.findAll()).thenReturn(List.of(m1, m2));

        List<MarcaResponse> response = marcaService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("Coca Cola", response.get(0).getNombre());
        assertEquals("Pepsi", response.get(1).getNombre());
    }

    @Test
    void obtenerPorId_deberiaRetornarMarca_cuandoExiste() {
        Marca marca = crearMarca(5L, "Inca Kola");
        when(marcaRepository.findById(5L)).thenReturn(Optional.of(marca));

        MarcaResponse response = marcaService.obtenerPorId(5L);

        assertEquals(5L, response.getId());
        assertEquals("Inca Kola", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(marcaRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> marcaService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Marca con id: 404 no encontrada", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        Marca existente = crearMarca(7L, "Marca Vieja");
        MarcaRequest request = new MarcaRequest("Marca Nueva");

        when(marcaRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(marcaRepository.existsByNombreAndIdNot("Marca Nueva", 7L)).thenReturn(false);
        when(marcaRepository.save(existente)).thenReturn(existente);

        MarcaResponse response = marcaService.actualizar(7L, request);

        assertEquals("Marca Nueva", existente.getNombre());
        assertEquals(7L, response.getId());
        assertEquals("Marca Nueva", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoMarcaNoExiste() {
        MarcaRequest request = new MarcaRequest("Marca Nueva");
        when(marcaRepository.findById(700L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> marcaService.actualizar(700L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(marcaRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        Marca existente = crearMarca(7L, "Marca Vieja");
        MarcaRequest request = new MarcaRequest("Marca Duplicada");

        when(marcaRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(marcaRepository.existsByNombreAndIdNot("Marca Duplicada", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> marcaService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe una Marca con ese nombre", ex.getMessage());
        verify(marcaRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminarMarca_cuandoExiste() {
        Marca marca = crearMarca(9L, "Marca Temporal");
        when(marcaRepository.findById(9L)).thenReturn(Optional.of(marca));

        marcaService.eliminar(9L);

        verify(marcaRepository).delete(marca);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(marcaRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> marcaService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Marca con id: 901 no encontrada", ex.getMessage());
        verify(marcaRepository, never()).delete(any());
    }

    private Marca crearMarca(Long id, String nombre) {
        Marca marca = new Marca();
        marca.setId(id);
        marca.setNombre(nombre);
        marca.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        marca.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 30));
        return marca;
    }
}