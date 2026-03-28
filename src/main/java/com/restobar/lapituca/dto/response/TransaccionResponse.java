package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionResponse {
    private Long id;
    private String mercadoPagoPaymentId;
    private String mercadoPagoPreferenceId;
    private String externalReference;
    private String estado;
    private String estadoMercadoPago;
    private String detalleEstadoMercadoPago;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaActualizacion;
    private Long usuarioId;
    private Long reservaId;
}