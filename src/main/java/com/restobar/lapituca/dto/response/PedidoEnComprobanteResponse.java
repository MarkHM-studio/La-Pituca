package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEnComprobanteResponse {
    private Long id;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String estado;
    private Long productoId;
    private String productoNombre;
    private Long tipoEntregaId;
    private String tipoEntregaNombre;
    private Long usuarioId;
    private String username;
}
