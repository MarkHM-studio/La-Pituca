package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInsumoListadoResponse {
    private Long id;
    private LocalDateTime fechaHora_registro;
    private BigDecimal cantidad;
    private String unidad_medida;
    private Long insumoId;
    private String insumoNombre;
    private Long comprobanteId;
}