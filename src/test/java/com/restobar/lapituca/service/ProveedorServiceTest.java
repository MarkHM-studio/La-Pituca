package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.ProveedorRequest;
import com.restobar.lapituca.dto.response.ProveedorResponse;
import com.restobar.lapituca.entity.Proveedor;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ProveedorRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestEsValido() {
        ProveedorRequest request = crearRequest(
                "Juan Perez", "Distribuidora Andina SAC", "20123456789",
                "Av. Principal 123", "987654321", "proveedor@test.com", "ACTIVO"
        );

        when(proveedorRepository.existsByCorreo("proveedor@test.com")).thenReturn(false);
        when(proveedorRepository.existsByRUC("20123456789")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(invocation -> {
            Proveedor p = invocation.getArgument(0);
            p.setId(10L);
            p.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
            p.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 15));
            return p;
        });

        ProveedorResponse response = proveedorService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals("Juan Perez", response.getContacto());
        assertEquals("Distribuidora Andina SAC", response.getRazonSocial());
        assertEquals("20123456789", response.getRuc());
        assertEquals("proveedor@test.com", response.getCorreo());
        assertEquals("ACTIVO", response.getEstado());
        assertNotNull(response.getFechaRegistro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoCorreoYaExiste() {
        ProveedorRequest request = crearRequest(
                "Juan Perez", "Distribuidora Andina SAC", "20123456789",
                "Av. Principal 123", "987654321", "proveedor@test.com", "ACTIVO"
        );

        when(proveedorRepository.existsByCorreo("proveedor@test.com")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> proveedorService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un proveedor con ese correo", ex.getMessage());
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoRucYaExiste() {
        ProveedorRequest request = crearRequest(
                "Juan Perez", "Distribuidora Andina SAC", "20123456789",
                "Av. Principal 123", "987654321", "proveedor@test.com", "ACTIVO"
        );

        when(proveedorRepository.existsByCorreo("proveedor@test.com")).thenReturn(false);
        when(proveedorRepository.existsByRUC("20123456789")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> proveedorService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un proveedor con ese RUC", ex.getMessage());
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaExcluirEliminados() {
        Proveedor activo = crearProveedor(1L, "ACTIVO", "activo@test.com", "20111111111");
        Proveedor eliminado = crearProveedor(2L, "ELIMINADO", "elim@test.com", "20222222222");

        when(proveedorRepository.findAll()).thenReturn(List.of(activo, eliminado));

        List<ProveedorResponse> response = proveedorService.listarTodos();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertTrue(response.stream().noneMatch(p -> "ELIMINADO".equalsIgnoreCase(p.getEstado())));
    }

    @Test
    void obtenerPorId_deberiaRetornarProveedor_cuandoExiste() {
        Proveedor proveedor = crearProveedor(5L, "ACTIVO", "ok@test.com", "20555555555");
        when(proveedorRepository.findById(5L)).thenReturn(Optional.of(proveedor));

        ProveedorResponse response = proveedorService.obtenerPorId(5L);

        assertEquals(5L, response.getId());
        assertEquals("ok@test.com", response.getCorreo());
        assertEquals("20555555555", response.getRuc());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(proveedorRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> proveedorService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Proveedor con id: 404 no encontrado", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        Proveedor proveedor = crearProveedor(7L, "ACTIVO", "old@test.com", "20777777777");
        ProveedorRequest request = crearRequest(
                "Maria Lopez", "Norte Supply SAC", "20999999999",
                "Jr. Norte 456", "912345678", "nuevo@test.com", "ACTIVO"
        );

        when(proveedorRepository.findById(7L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.existsByCorreoAndIdNot("nuevo@test.com", 7L)).thenReturn(false);
        when(proveedorRepository.existsByRUCAndIdNot("20999999999", 7L)).thenReturn(false);
        when(proveedorRepository.save(proveedor)).thenReturn(proveedor);

        ProveedorResponse response = proveedorService.actualizar(7L, request);

        assertEquals("Maria Lopez", proveedor.getContacto());
        assertEquals("Norte Supply SAC", proveedor.getRazon_social());
        assertEquals("20999999999", proveedor.getRUC());
        assertEquals("nuevo@test.com", proveedor.getCorreo());
        assertEquals(7L, response.getId());
        assertEquals("Maria Lopez", response.getContacto());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoProveedorNoExiste() {
        ProveedorRequest request = crearRequest(
                "Maria Lopez", "Norte Supply SAC", "20999999999",
                "Jr. Norte 456", "912345678", "nuevo@test.com", "ACTIVO"
        );

        when(proveedorRepository.findById(700L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> proveedorService.actualizar(700L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoCorreoDuplicado() {
        Proveedor proveedor = crearProveedor(7L, "ACTIVO", "old@test.com", "20777777777");
        ProveedorRequest request = crearRequest(
                "Maria Lopez", "Norte Supply SAC", "20999999999",
                "Jr. Norte 456", "912345678", "duplicado@test.com", "ACTIVO"
        );

        when(proveedorRepository.findById(7L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.existsByCorreoAndIdNot("duplicado@test.com", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> proveedorService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un proveedor con ese correo", ex.getMessage());
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoRucDuplicado() {
        Proveedor proveedor = crearProveedor(7L, "ACTIVO", "old@test.com", "20777777777");
        ProveedorRequest request = crearRequest(
                "Maria Lopez", "Norte Supply SAC", "20999999999",
                "Jr. Norte 456", "912345678", "nuevo@test.com", "ACTIVO"
        );

        when(proveedorRepository.findById(7L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.existsByCorreoAndIdNot("nuevo@test.com", 7L)).thenReturn(false);
        when(proveedorRepository.existsByRUCAndIdNot("20999999999", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> proveedorService.actualizar(7L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un proveedor con ese RUC", ex.getMessage());
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaCambiarEstadoAEliminado() {
        Proveedor proveedor = crearProveedor(9L, "ACTIVO", "delete@test.com", "20999999991");
        when(proveedorRepository.findById(9L)).thenReturn(Optional.of(proveedor));

        proveedorService.eliminar(9L);

        assertEquals("ELIMINADO", proveedor.getEstado());
        verify(proveedorRepository).save(proveedor);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(proveedorRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> proveedorService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Proveedor con id: 901 no encontrado", ex.getMessage());
        verify(proveedorRepository, never()).save(any());
    }

    private ProveedorRequest crearRequest(String contacto, String razonSocial, String ruc, String direccion,
                                          String telefono, String correo, String estado) {
        return new ProveedorRequest(contacto, razonSocial, ruc, direccion, telefono, correo, estado);
    }

    private Proveedor crearProveedor(Long id, String estado, String correo, String ruc) {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(id);
        proveedor.setContacto("Contacto " + id);
        proveedor.setRazon_social("Razon Social " + id);
        proveedor.setRUC(ruc);
        proveedor.setDireccion("Direccion " + id);
        proveedor.setTelefono("987654321");
        proveedor.setCorreo(correo);
        proveedor.setEstado(estado);
        proveedor.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        proveedor.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 30));
        return proveedor;
    }
}