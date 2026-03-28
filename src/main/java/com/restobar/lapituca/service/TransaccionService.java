package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.response.TransaccionResponse;
import com.restobar.lapituca.entity.Transaccion;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;

    public List<TransaccionResponse> listarTodos() {
        return transaccionRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TransaccionResponse obtenerPorId(Long id) {
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Transacción con id: " + id + " no encontrada"));

        return toResponse(transaccion);
    }

    private TransaccionResponse toResponse(Transaccion transaccion) {
        return new TransaccionResponse(
                transaccion.getId(),
                transaccion.getMercadoPagoPaymentId(),
                transaccion.getMercadoPagoPreferenceId(),
                transaccion.getExternalReference(),
                transaccion.getEstado(),
                transaccion.getEstadoMercadoPago(),
                transaccion.getDetalleEstadoMercadoPago(),
                transaccion.getMonto(),
                transaccion.getFechaPago(),
                transaccion.getFechaActualizacion(),
                transaccion.getUsuario() != null ? transaccion.getUsuario().getId() : null,
                transaccion.getReserva() != null ? transaccion.getReserva().getId() : null
        );
    }
}