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

    @NotBlank(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante; // BOLETA o FACTURA

    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    private String dni;

    @Size(min = 11, max = 11, message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;
}