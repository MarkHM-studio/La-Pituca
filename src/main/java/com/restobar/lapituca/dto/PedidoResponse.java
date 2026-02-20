package com.restobar.lapituca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {

    private Long id;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String estado;
    private LocalDateTime fechaRegistro;

    private Long comprobanteId;
    private Long productoId;
    private Long tipoEntregaId;
}
