package com.restobar.lapituca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDetalleResponse {
    private Long id;
    private Integer cantidad;
    private BigDecimal subtotal;
    private String estado;
    private LocalDateTime fechaHoraRegistro;
    private ProductoResponse producto;      // DTO
    private ComprobanteResponse comprobante; // DTO
}