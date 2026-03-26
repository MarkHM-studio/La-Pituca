package com.restobar.lapituca.dto.request;
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
public class TransaccionRequest {

    @NotBlank(message = "mercadoPagoPaymentId es obligatorio")
    private String mercadoPagoPaymentId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    @NotNull(message = "Usuario obligatorio")
    private Long usuarioId;

    @NotNull(message = "Reserva obligatoria")
    private Long reservaId;
}