package com.restobar.lapituca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarVentaRequest {
    private Long usuarioId;
    private Long comprobanteId;
    private Set<Long> tipoPagoId;
    private List<BigDecimal> montos;
    private Long tipoBilleteraVirtualId;
}
