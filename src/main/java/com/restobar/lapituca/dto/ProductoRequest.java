package com.restobar.lapituca.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 5, max = 50, message = "El nombre debe tener entre 5 y 50 caracteres")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    @Digits(integer = 5, fraction = 2, message = "El precio no puede tener más de 5 dígitos enteros y 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private Long categoriaId;
    private Long marcaId;
}
