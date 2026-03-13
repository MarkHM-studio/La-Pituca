package com.restobar.lapituca.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarVentaRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El comprobante es obligatorio")
    private Long comprobanteId;

    @NotEmpty(message = "Debe indicar al menos un tipo de pago")
    private Set<@NotNull(message = "El tipo de pago no puede ser nulo") Long> tipoPagoId;

    @NotEmpty(message = "Debe indicar los montos de pago")
    private List<
            @NotNull(message = "El monto no puede ser nulo")
            @Positive(message = "El monto debe ser positivo")
                    BigDecimal
            > montos;

    private Long tipoBilleteraVirtualId;

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;
}