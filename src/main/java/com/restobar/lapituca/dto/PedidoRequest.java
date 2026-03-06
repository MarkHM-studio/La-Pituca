package com.restobar.lapituca.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Set;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PedidoRequest {

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    @Digits(integer = 3, fraction = 0, message = "La cantidad no puede tener decimales y máximo 3 dígitos")
    private Integer cantidad;

    @NotNull(message = "El comprobante es obligatorio")
    @Positive(message = "El id del comprobante debe ser mayor a 0")
    private Long comprobanteId;

    @NotNull(message = "El producto es obligatorio")
    @Positive(message = "El id del producto debe ser mayor a 0")
    private Long productoId;

    @NotNull(message = "El tipo de entrega es obligatorio")
    @Positive(message = "El id del tipo de entrega debe ser mayor a 0")
    private Long tipoEntregaId;

}
