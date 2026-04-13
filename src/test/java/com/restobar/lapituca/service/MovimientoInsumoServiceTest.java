package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.response.MovimientoInsumoDetalleResponse;
import com.restobar.lapituca.dto.response.MovimientoInsumoListadoResponse;
import com.restobar.lapituca.entity.Comprobante;
import com.restobar.lapituca.entity.Insumo;
import com.restobar.lapituca.entity.MovimientoInsumo;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.MovimientoInsumoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoInsumoServiceTest {

    @Mock
    private MovimientoInsumoRepository movimientoInsumoRepository;

    @InjectMocks
    private MovimientoInsumoService movimientoInsumoService;

    @Test
    void listar_deberiaRetornarListaOrdenada() {
        MovimientoInsumo m1 = crearMovimiento(1L);
        MovimientoInsumo m2 = crearMovimiento(2L);

        when(movimientoInsumoRepository.listarTodosOrdenadosPorRegistroDesc())
                .thenReturn(List.of(m1, m2));

        List<MovimientoInsumoListadoResponse> response = movimientoInsumoService.listar();

        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals("Insumo Test", response.get(0).getInsumoNombre());
        assertEquals(1L, response.get(0).getInsumoId());
        assertEquals(1L, response.get(0).getComprobanteId());
    }

    @Test
    void obtenerDetalle_deberiaRetornarDetalleCompleto_cuandoExiste() {
        MovimientoInsumo movimiento = crearMovimiento(10L);

        when(movimientoInsumoRepository.findById(10L))
                .thenReturn(Optional.of(movimiento));

        MovimientoInsumoDetalleResponse response = movimientoInsumoService.obtenerDetalle(10L);

        assertEquals(10L, response.getId());
        assertEquals(new BigDecimal("5.00"), response.getCantidad());

        // insumo
        assertNotNull(response.getInsumo());
        assertEquals(1L, response.getInsumo().getId());
        assertEquals("Insumo Test", response.getInsumo().getNombre());

        // comprobante
        assertNotNull(response.getComprobante());
        assertEquals(1L, response.getComprobante().getId());
        assertEquals(new BigDecimal("100.00"), response.getComprobante().getTotal());
    }

    @Test
    void obtenerDetalle_deberiaRetornarNulls_cuandoNoHayRelaciones() {
        MovimientoInsumo movimiento = crearMovimiento(20L);
        movimiento.setInsumo(null);
        movimiento.setComprobante(null);

        when(movimientoInsumoRepository.findById(20L))
                .thenReturn(Optional.of(movimiento));

        MovimientoInsumoDetalleResponse response = movimientoInsumoService.obtenerDetalle(20L);

        assertNull(response.getInsumo());
        assertNull(response.getComprobante());
    }

    @Test
    void obtenerDetalle_deberiaLanzarError_cuandoNoExiste() {
        when(movimientoInsumoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> movimientoInsumoService.obtenerDetalle(99L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("MovimientoInsumo con id: 99 no encontrado", ex.getMessage());
    }

    // 🔧 helper
    private MovimientoInsumo crearMovimiento(Long id) {
        MovimientoInsumo m = new MovimientoInsumo();
        m.setId(id);
        m.setCantidad(new BigDecimal("5.00"));
        m.setUnidad_medida("kg");
        m.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
        m.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 11, 0));

        // insumo
        Insumo insumo = new Insumo();
        insumo.setId(1L);
        insumo.setNombre("Insumo Test");
        insumo.setUnidad_medida("kg");
        insumo.setStock(new BigDecimal("50.00"));

        // comprobante
        Comprobante comprobante = new Comprobante();
        comprobante.setId(1L);
        comprobante.setTotal(new BigDecimal("100.00"));
        comprobante.setEstado("PAGADO");
        comprobante.setFechaHora_venta(LocalDateTime.of(2026, 4, 1, 9, 0));

        m.setInsumo(insumo);
        m.setComprobante(comprobante);

        return m;
    }
}