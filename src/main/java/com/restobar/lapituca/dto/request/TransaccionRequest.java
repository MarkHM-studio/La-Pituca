package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionRequest {

    @NotNull(message = "MercadoPagoId es obligatorio")
    private Long mercadoPagoId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    @NotNull(message = "Usuario obligatorio")
    private Long usuarioId;

    @NotNull(message = "Reserva obligatoria")
    private Long reservaId;
}