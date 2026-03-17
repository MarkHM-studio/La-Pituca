package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EntradaResponse {
    private Long id;
    private Long productoId;
    private Long insumoId;
    private BigDecimal cantidadTotal;
    private String unidadMedida;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private Long proveedorId;
    private Long usuarioId;
    private LocalDateTime fechaRegistro;
}
