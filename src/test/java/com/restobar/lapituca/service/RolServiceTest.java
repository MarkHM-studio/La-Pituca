package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.RolRequest;
import com.restobar.lapituca.dto.response.RolResponse;
import com.restobar.lapituca.entity.Rol;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.RolRepository;
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
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolService rolService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestValido() {
        RolRequest request = new RolRequest("ADMIN");

        when(rolRepository.existsByNombre("ADMIN")).thenReturn(false);
        when(rolRepository.save(any(Rol.class))).thenAnswer(invocation -> {
            Rol rol = invocation.getArgument(0);
            rol.setId(10L);
            rol.setFechaHora_registro(LocalDateTime.of(2026, 4, 12, 13, 0));
            rol.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 12, 13, 15));
            return rol;
        });

        RolResponse response = rolService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals("ADMIN", response.getNombre());
        assertNotNull(response.getFechaHora_registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        RolRequest request = new RolRequest("ADMIN");
        when(rolRepository.existsByNombre("ADMIN")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> rolService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Rol con ese nombre", ex.getMessage());
        verify(rolRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarRolesMapeados() {
        Rol r1 = crearRol(1L, "ADMIN");
        Rol r2 = crearRol(2L, "CAJERO");

        when(rolRepository.findAll()).thenReturn(List.of(r1, r2));

        List<RolResponse> response = rolService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("ADMIN", response.get(0).getNombre());
        assertEquals("CAJERO", response.get(1).getNombre());
    }

    @Test
    void obtenerPorId_deberiaRetornarRol_cuandoExiste() {
        Rol rol = crearRol(5L, "MOZO");
        when(rolRepository.findById(5L)).thenReturn(Optional.of(rol));

        RolResponse response = rolService.obtenerPorId(5L);

        assertEquals(5L, response.getId());
        assertEquals("MOZO", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(rolRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> rolService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Rol con id: 404 no encontrado", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        Rol existente = crearRol(7L, "ROL_ANTIGUO");
        RolRequest request = new RolRequest("ROL_NUEVO");

        when(rolRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(rolRepository.existsByNombreAndIdNot("ROL_NUEVO", 7L)).thenReturn(false);
        when(rolRepository.save(existente)).thenReturn(existente);

        RolResponse response = rolService.actualizar(7L, request);

        assertEquals("ROL_NUEVO", existente.getNombre());
        assertEquals(7L, response.getId());
        assertEquals("ROL_NUEVO", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoRolNoExiste() {
        RolRequest request = new RolRequest("ROL_NUEVO");
        when(rolRepository.findById(700L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> rolService.actualizar(700L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(rolRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        Rol existente = crearRol(7L, "ROL_ANTIGUO");
        RolRequest request = new RolRequest("ROL_DUPLICADO");

        when(rolRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(rolRepository.existsByNombreAndIdNot("ROL_DUPLICADO", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> rolService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Rol con ese nombre", ex.getMessage());
        verify(rolRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminarRol_cuandoExiste() {
        Rol rol = crearRol(9L, "ROL_EVENTUAL");
        when(rolRepository.findById(9L)).thenReturn(Optional.of(rol));

        rolService.eliminar(9L);

        verify(rolRepository).delete(rol);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(rolRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> rolService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Rol con id:901 no encontrado", ex.getMessage());
        verify(rolRepository, never()).delete(any());
    }

    private Rol crearRol(Long id, String nombre) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        rol.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        rol.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 30));
        return rol;
    }
}