package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInsumoDetalleResponse {
    private Long id;
    private BigDecimal cantidad;
    private String unidad_medida;
    private LocalDateTime fechaHora_registro;
    private LocalDateTime fechaHora_actualizacion;
    private InsumoDetalleEnMovimientoResponse insumo;
    private ComprobanteResumenEnMovimientoResponse comprobante;
}