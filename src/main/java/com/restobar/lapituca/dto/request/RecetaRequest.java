package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
