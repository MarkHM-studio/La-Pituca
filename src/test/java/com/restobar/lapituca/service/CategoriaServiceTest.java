package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.CategoriaRequest;
import com.restobar.lapituca.dto.response.CategoriaResponse;
import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.CategoriaRepository;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestValido() {
        CategoriaRequest request = new CategoriaRequest("BEBIDAS");

        when(categoriaRepository.existsByNombre("BEBIDAS")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria categoria = invocation.getArgument(0);
            categoria.setId(10L);
            categoria.setFechaHora_registro(LocalDateTime.of(2026, 4, 12, 16, 0));
            categoria.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 12, 16, 15));
            return categoria;
        });

        CategoriaResponse response = categoriaService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals("BEBIDAS", response.getNombre());
        assertNotNull(response.getFechaHora_Registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        CategoriaRequest request = new CategoriaRequest("BEBIDAS");
        when(categoriaRepository.existsByNombre("BEBIDAS")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> categoriaService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe una Categoría con ese nombre", ex.getMessage());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarCategoriasMapeadas() {
        Categoria c1 = crearCategoria(1L, "BEBIDAS");
        Categoria c2 = crearCategoria(2L, "COMIDA");

        when(categoriaRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CategoriaResponse> response = categoriaService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("BEBIDAS", response.get(0).getNombre());
        assertEquals("COMIDA", response.get(1).getNombre());
    }

    @Test
    void obtenerPorId_deberiaRetornarCategoria_cuandoExiste() {
        Categoria categoria = crearCategoria(5L, "POSTRES");
        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(categoria));

        CategoriaResponse response = categoriaService.obtenerPorId(5L);

        assertEquals(5L, response.getId());
        assertEquals("POSTRES", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(categoriaRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> categoriaService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Categoria con id: 404 no encontrada", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        Categoria existente = crearCategoria(7L, "CAT_ANTIGUA");
        CategoriaRequest request = new CategoriaRequest("CAT_NUEVA");

        when(categoriaRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.existsByNombreAndIdNot("CAT_NUEVA", 7L)).thenReturn(false);
        when(categoriaRepository.save(existente)).thenReturn(existente);

        CategoriaResponse response = categoriaService.actualizar(7L, request);

        assertEquals("CAT_NUEVA", existente.getNombre());
        assertEquals(7L, response.getId());
        assertEquals("CAT_NUEVA", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoCategoriaNoExiste() {
        CategoriaRequest request = new CategoriaRequest("CAT_NUEVA");
        when(categoriaRepository.findById(700L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> categoriaService.actualizar(700L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        Categoria existente = crearCategoria(7L, "CAT_ANTIGUA");
        CategoriaRequest request = new CategoriaRequest("CAT_DUPLICADA");

        when(categoriaRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.existsByNombreAndIdNot("CAT_DUPLICADA", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> categoriaService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe una categoría con ese nombre", ex.getMessage());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminarCategoria_cuandoExiste() {
        Categoria categoria = crearCategoria(9L, "CAT_TEMP");
        when(categoriaRepository.findById(9L)).thenReturn(Optional.of(categoria));

        categoriaService.eliminar(9L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(categoriaRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> categoriaService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Categoria con id: 901 no encontrada", ex.getMessage());
        verify(categoriaRepository, never()).delete(any());
    }

    private Categoria crearCategoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        categoria.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 30));
        return categoria;
    }
}
