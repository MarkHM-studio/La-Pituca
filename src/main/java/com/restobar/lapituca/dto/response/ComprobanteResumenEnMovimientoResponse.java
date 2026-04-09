package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteResumenEnMovimientoResponse {
    private Long id;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fechaHora_venta;
}