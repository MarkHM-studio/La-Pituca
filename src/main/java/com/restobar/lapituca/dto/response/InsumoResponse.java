package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsumoResponse {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private BigDecimal stock;
    private String unidadMedida;
    private Long marcaId;
}
