package com.restobar.lapituca.dto.request.mercadopago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPreferenciaPagoRequest {

    @NotNull(message = "reservaId es obligatorio")
    private Long reservaId;

    @NotBlank(message = "descripcion es obligatoria")
    private String descripcion;

    @NotNull(message = "monto es obligatorio")
    @Positive(message = "monto debe ser positivo")
    private BigDecimal monto;
}