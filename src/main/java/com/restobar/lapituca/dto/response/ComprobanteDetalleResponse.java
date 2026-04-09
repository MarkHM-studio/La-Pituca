package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteDetalleResponse {
    private Long id;
    private BigDecimal total;
    private BigDecimal IGV;
    private BigDecimal subtotal;
    private LocalDateTime fechaHora_apertura;
    private LocalDateTime fechaHora_venta;
    private String estado;
    private SucursalResumenResponse sucursal;
    private UsuarioResumenResponse cajero;
    private GrupoDetalleResponse grupo;
    private ReservaDetalleEnComprobanteResponse reserva;
    private List<PedidoEnComprobanteResponse> pedidos;
    private List<MovimientoTipoPagoDetalleResponse> movimientosTipoPago;
}