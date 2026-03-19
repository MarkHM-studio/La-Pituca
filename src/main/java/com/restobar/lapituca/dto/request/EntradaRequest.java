package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
