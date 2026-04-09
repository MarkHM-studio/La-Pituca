package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoTipoPagoDetalleResponse {
    private Long id;
    private BigDecimal monto;
    private Long tipoPagoId;
    private String tipoPagoNombre;
    private Long tipoBilleteraVirtualId;
    private String tipoBilleteraVirtualNombre;
}