package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EntradaRequest {

    private Long productoId;
    private Long insumoId;

    @NotNull
    @Positive
    private BigDecimal cantidadTotal;

    @NotBlank
    private String unidadMedida;

    @NotNull
    @Positive
    private BigDecimal costoUnitario;

    @NotNull
    @Positive
    private Long proveedorId;

    @NotNull
    @Positive
    private Long usuarioId;
}
