package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsumoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50)
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal precio;

    @DecimalMin(value = "0.0", inclusive = true, message = "El stock no puede ser negativo")
    private BigDecimal stock;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;

    private Long marcaId;
}
