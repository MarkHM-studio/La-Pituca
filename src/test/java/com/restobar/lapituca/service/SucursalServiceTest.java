package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.SucursalRequest;
import com.restobar.lapituca.dto.response.SucursalResponse;
import com.restobar.lapituca.entity.Sucursal;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.SucursalRepository;
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
class SucursalServiceTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private SucursalService sucursalService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestEsValido() {
        SucursalRequest request = new SucursalRequest("Sucursal Centro", "Av. Principal 123", "20123456789");

        when(sucursalRepository.existsByNombre("Sucursal Centro")).thenReturn(false);
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(invocation -> {
            Sucursal s = invocation.getArgument(0);
            s.setId(10L);
            s.setFechaHora_registro(LocalDateTime.of(2026, 4, 12, 10, 0));
            s.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 12, 10, 15));
            return s;
        });

        SucursalResponse response = sucursalService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals("Sucursal Centro", response.getNombre());
        assertEquals("Av. Principal 123", response.getDireccion());
        assertEquals("20123456789", response.getRUC());
        assertNotNull(response.getFechaHora_registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        SucursalRequest request = new SucursalRequest("Sucursal Centro", "Av. Principal 123", "20123456789");
        when(sucursalRepository.existsByNombre("Sucursal Centro")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> sucursalService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe una Sucursal con ese nombre", ex.getMessage());
        verify(sucursalRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarTodasLasSucursalesMapeadas() {
        Sucursal s1 = crearSucursal(1L, "Sucursal Centro", "Av. Principal 123", "20111111111");
        Sucursal s2 = crearSucursal(2L, "Sucursal Norte", "Jr. Lima 456", "20222222222");

        when(sucursalRepository.findAll()).thenReturn(List.of(s1, s2));

        List<SucursalResponse> response = sucursalService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("Sucursal Centro", response.get(0).getNombre());
        assertEquals("Sucursal Norte", response.get(1).getNombre());
    }

    @Test
    void obtenerPorId_deberiaRetornarSucursal_cuandoExiste() {
        Sucursal sucursal = crearSucursal(5L, "Sucursal Sur", "Av. Sur 100", "20555555555");
        when(sucursalRepository.findById(5L)).thenReturn(Optional.of(sucursal));

        SucursalResponse response = sucursalService.obtenerPorId(5L);

        assertEquals(5L, response.getId());
        assertEquals("Sucursal Sur", response.getNombre());
        assertEquals("20555555555", response.getRUC());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(sucursalRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> sucursalService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Sucursal con id: 404 no encontrada", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        Sucursal existente = crearSucursal(7L, "Sucursal Antiguo", "Av. Vieja 1", "20777777777");
        SucursalRequest request = new SucursalRequest("Sucursal Nueva", "Av. Nueva 99", "20999999999");

        when(sucursalRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(sucursalRepository.existsByNombreAndIdNot("Sucursal Nueva", 7L)).thenReturn(false);
        when(sucursalRepository.save(existente)).thenReturn(existente);

        SucursalResponse response = sucursalService.actualizar(7L, request);

        assertEquals("Sucursal Nueva", existente.getNombre());
        assertEquals("Av. Nueva 99", existente.getDireccion());
        assertEquals("20999999999", existente.getRUC());

        assertEquals(7L, response.getId());
        assertEquals("Sucursal Nueva", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoSucursalNoExiste() {
        SucursalRequest request = new SucursalRequest("Sucursal Nueva", "Av. Nueva 99", "20999999999");
        when(sucursalRepository.findById(700L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> sucursalService.actualizar(700L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(sucursalRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        Sucursal existente = crearSucursal(7L, "Sucursal Antiguo", "Av. Vieja 1", "20777777777");
        SucursalRequest request = new SucursalRequest("Sucursal Duplicada", "Av. Nueva 99", "20999999999");

        when(sucursalRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(sucursalRepository.existsByNombreAndIdNot("Sucursal Duplicada", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> sucursalService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe una Sucursal con ese nombre", ex.getMessage());
        verify(sucursalRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminarSucursal_cuandoExiste() {
        Sucursal sucursal = crearSucursal(9L, "Sucursal Este", "Av. Este 10", "20999999991");
        when(sucursalRepository.findById(9L)).thenReturn(Optional.of(sucursal));

        sucursalService.eliminar(9L);

        verify(sucursalRepository).delete(sucursal);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(sucursalRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> sucursalService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Sucursal con id:901 no encontrada", ex.getMessage());
        verify(sucursalRepository, never()).delete(any());
    }

    private Sucursal crearSucursal(Long id, String nombre, String direccion, String ruc) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(id);
        sucursal.setNombre(nombre);
        sucursal.setDireccion(direccion);
        sucursal.setRUC(ruc);
        sucursal.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        sucursal.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 30));
        return sucursal;
    }
}