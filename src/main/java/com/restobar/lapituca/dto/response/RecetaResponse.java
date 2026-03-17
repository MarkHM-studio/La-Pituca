package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecetaResponse {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Long insumoId;
    private String insumoNombre;
    private BigDecimal cantidad;
    private String unidadMedida;
}
