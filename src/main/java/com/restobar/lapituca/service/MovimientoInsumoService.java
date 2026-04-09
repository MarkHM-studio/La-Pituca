package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.response.*;
import com.restobar.lapituca.entity.MovimientoInsumo;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.MovimientoInsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoInsumoService {

    private final MovimientoInsumoRepository movimientoInsumoRepository;

    public List<MovimientoInsumoListadoResponse> listar() {
        return movimientoInsumoRepository.listarTodosOrdenadosPorRegistroDesc().stream()
                .map(this::toListadoResponse)
                .toList();
    }

    public MovimientoInsumoDetalleResponse obtenerDetalle(Long id) {
        MovimientoInsumo movimiento = movimientoInsumoRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "MovimientoInsumo con id: " + id + " no encontrado"));

        return toDetalleResponse(movimiento);
    }

    private MovimientoInsumoListadoResponse toListadoResponse(MovimientoInsumo movimiento) {
        return new MovimientoInsumoListadoResponse(
                movimiento.getId(),
                movimiento.getFechaHora_registro(),
                movimiento.getCantidad(),
                movimiento.getUnidad_medida(),
                movimiento.getInsumo() != null ? movimiento.getInsumo().getId() : null,
                movimiento.getInsumo() != null ? movimiento.getInsumo().getNombre() : null,
                movimiento.getComprobante() != null ? movimiento.getComprobante().getId() : null
        );
    }

    private MovimientoInsumoDetalleResponse toDetalleResponse(MovimientoInsumo movimiento) {
        InsumoDetalleEnMovimientoResponse insumo = null;
        if (movimiento.getInsumo() != null) {
            insumo = new InsumoDetalleEnMovimientoResponse(
                    movimiento.getInsumo().getId(),
                    movimiento.getInsumo().getNombre(),
                    movimiento.getInsumo().getUnidad_medida(),
                    movimiento.getInsumo().getStock()
            );
        }

        ComprobanteResumenEnMovimientoResponse comprobante = null;
        if (movimiento.getComprobante() != null) {
            comprobante = new ComprobanteResumenEnMovimientoResponse(
                    movimiento.getComprobante().getId(),
                    movimiento.getComprobante().getTotal(),
                    movimiento.getComprobante().getEstado(),
                    movimiento.getComprobante().getFechaHora_venta()
            );
        }

        return new MovimientoInsumoDetalleResponse(
                movimiento.getId(),
                movimiento.getCantidad(),
                movimiento.getUnidad_medida(),
                movimiento.getFechaHora_registro(),
                movimiento.getFechaHora_actualizacion(),
                insumo,
                comprobante
        );
    }
}