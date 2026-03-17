package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class InsumoResponse {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private BigDecimal stock;
    private String unidadMedida;
    private Long marcaId;
}
