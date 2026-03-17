package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecetaRequest {

    @NotNull
    @Positive
    private Long productoId;

    @NotEmpty
    private List<@NotNull @Positive Long> insumosId;

    @NotEmpty
    private List<@NotNull @Positive BigDecimal> cantidades;

    @NotEmpty
    private List<@NotBlank String> unidadesMedida;
}
