package com.restobar.lapituca.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequest {

    @Size(min = 5, max = 50)
    private String nombre;

    @Positive
    @Digits(integer = 5, fraction = 2)
    private BigDecimal precio;

    @Min(0)
    private Integer stock;

    private Long categoriaId;
    private Long marcaId;
}
